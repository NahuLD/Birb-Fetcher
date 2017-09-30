package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.Pair;
import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.api.json.ImageSource;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import com.proximyst.birbfetcher.reddit.api.json.PostData;
import com.proximyst.birbfetcher.reddit.api.json.PostImage;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class ProcessingThread
			extends Thread {
	private static MessageDigest digest;

	private final RedditFetcher fetcher;
	private final FetchThread master;

	static {
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			digest = null;
		}
	}

	@Override
	public void run() {
		if (digest == null) {
			return;
		}
		File directory = new File(fetcher.getConfiguration().getBirbDirectory());
		Post post;
		while ((post = master.getBirbPosts().poll()) != null) {
			if (post.getData() == null) { // Shouldn't ever, but it may at some point.
				continue;
			}
			PostData data = post.getData();
			if (data.isHidden() // Reddit hidden posts are not allowed
						|| data.isQuarantine() // Might be infected or such. mods decide this
						|| (data.getBanned_by() != null // If the user is banned we don't want the image.
						&& !data.getBanned_by().equals("")) // Make sure they were actually banned
						|| !data.getPost_hint().equalsIgnoreCase("image") // Make sure it's an image
						|| !data.getSubreddit_type().equalsIgnoreCase("public") // Make sure it's public
						|| (data.getPreview().getImages() == null
						|| data.getPreview().getImages().size() <= 0)) {
				continue;
			}

			// Image URLs from ImageSource are always https://i.redditmedia.com
			data.getPreview().getImages().parallelStream()
					.map(PostImage::getSource)
					.map(ImageSource::getUrl)
					.map(in -> {
						final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						try {
							URL url = new URL(in);
							HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
							connection.setConnectTimeout(3000);
							connection.setReadTimeout(3000);
							connection.connect(); // Force connection.
							try (InputStream read = connection.getInputStream()) {
								byte[] buffer = new byte[1024];
								int length;
								while ((length = read.read(buffer)) > 0) {
									outputStream.write(buffer, 0, length);
								}
							}
						} catch (IOException | ClassCastException e) {
							return null;
						}
						return outputStream.toByteArray();
					})
//					.filter(Objects::nonNull) - Doing it in the next map is more efficient.
						.<Map.Entry<String, Pair<byte[], File>>>map(in -> {
							if (in == null || in.length <= 0) {
								return null;
							}
							byte[] sig = digest.digest(in);
							StringBuilder builder = new StringBuilder();
							for (byte digestedByte : sig) {
								builder.append(Integer.toString(
											(digestedByte & 0xff) + 0x100, 16
								).substring(1));
								if (builder.length() == 16) {
									break;
								}
							}
							String digest = builder.toString();
							File file = new File(directory, digest);
							if (file.exists()) { // Chances for duplicate signatures of which pow(62, 16) match are really low.
								return null; // pretty safe to say it's the same image.
							}
							try {
								if (!file.createNewFile()) {
									return null;
								}
							} catch (IOException e) {
								e.printStackTrace(); // Should never happen in prod, so let's see what the error is.
								return null;
							}
							return new AbstractMap.SimpleImmutableEntry<>(digest, new Pair<>(in, file));
						})
						.filter(Objects::nonNull) // An error or duplicate occurred. Drop them.
						.filter(entry -> !master.getFiles().containsKey(entry.getKey()))
						.forEach(entry -> {
							try (FileOutputStream stream = new FileOutputStream(entry.getValue().getB())) {
								stream.write(entry.getValue().getA());
							} catch (IOException ignored) {
								entry.getValue().getB().delete(); // Corrupt file after this stage.
							}
						});
		}
	}
}
