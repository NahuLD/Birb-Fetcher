package com.proximyst.birbfetcher.threading;

import com.proximyst.birbfetcher.Utilities;
import com.proximyst.birbfetcher.api.post.Post;
import com.proximyst.birbfetcher.data.Pair;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class PostProcessingSlave
			extends Thread {
	private final PostFetchingThread master;
	private final File directory;

	@Override
	public void run() {
		Post post;
		while ((post = master.getPostQueue().poll()) != null) {
			if (isInterrupted()) {
				break;
				// Do not process if it's interrupted. Ever.
				// Most likely if it is after poll, it's a shutdown so it doesn't matter.
			}
			Post.PostData data = post.getData();
			// Security purposes.
			if (data == null // Reddit may be weird...
						|| data.isHidden() // We don't have access to it, and/or may be non-related
						|| data.isQuarantine() // Virus? Something sketchy at least.
						|| (data.getBanned_by() != null && !data.getBanned_by().equals("")) // Banned? Shoo shoo
						|| data.getPreview() == null // Might happen. Text posts are examples.
						|| data.getPreview().getImages() == null // Make sure there are actually image list
						|| data.getPreview().getImages().isEmpty() // Make sure there are actually images
						|| !data.getSubreddit_type().equalsIgnoreCase("public")) { // Make sure it's public.
				continue;
			}

			data.getPreview().getImages()
					.parallelStream()
					.map(Post.PostImage::getSource)
					.map(Post.ImageSource::getUrl)
					.map(url -> new Pair<>(
								url.substring(url.lastIndexOf('.')),
								Utilities.readUrl(url)
					))
					.filter(pair -> pair.getB() != null && pair.getB().length > 0)
					.map(pair -> {
						String extension = pair.getA();
						while (extension.charAt(0) == '.') {
							extension = extension.substring(1);
						}
						extension = extension.split("[^a-zA-Z0-9]", 2)[0];
						byte[] fileData = pair.getB();
						byte[] digest = Utilities.getSignature(fileData);
						String filename = Utilities.getFileName(digest) + '.' + extension;
						File file = new File(
									directory,
									filename
						);
						return new Pair<>(
									file,
									fileData
						);
					})
					.filter(pair -> !pair.getA().exists())
					.forEach(pair -> {
						try (OutputStream stream = new FileOutputStream(pair.getA())) {
							stream.write(pair.getB());
						} catch (IOException e) {
							println("Couldn't write to file!");
							e.printStackTrace();
						}
					});
		}
		master.getSlaves().remove(this);
		println("Slave returning due to no more posts!");
	}
}
