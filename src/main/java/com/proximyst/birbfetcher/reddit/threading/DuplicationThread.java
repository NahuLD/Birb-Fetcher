package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static com.proximyst.birbfetcher.reddit.Utilities.println;

@RequiredArgsConstructor
public class DuplicationThread
			extends Thread {
	private final RedditFetcher fetcher;

	@Override
	public void run() {
		File directory = new File('.' + File.separator + fetcher.getConfiguration().getBirbDirectory());
		File[] files = directory.listFiles(); // safe as it returns null if it isn't a directory.
		if (files == null || files.length <= 0) {
			println("No files were found.");
			return; // No duplicates because of no files.
		}
		Map<String, byte[]> fileData = new HashMap<>(); // Only one image per signature.

		for (File file : files) {
			if (!file.isFile()) {
				file.delete();
				continue;
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (InputStream stream = new FileInputStream(file)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = stream.read(buffer)) >= 0) {
					bytes.write(buffer, 0, length);
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			byte[] imageBytes = bytes.toByteArray();
			byte[] digestedBytes = getDigest().digest(imageBytes);
			final StringBuilder builder = new StringBuilder();
			for (byte digestedByte : digestedBytes) {
				builder.append(Integer.toString(
							(digestedByte & 0xff) + 0x100, 16
				).substring(1));
				if (builder.length() >= 16) {
					break;
				}
			}
			String filename = file.getName();
			int lastIndex = filename.lastIndexOf('.');
			if (lastIndex == -1) {
				if (filename.endsWith("gif")) {
					lastIndex = filename.lastIndexOf('g');
				} else if (filename.endsWith("png")) {
					lastIndex = filename.lastIndexOf('p');
				} else if (filename.endsWith("svg")) {
					lastIndex = filename.lastIndexOf('g');
				} else if (filename.endsWith("g")) {
					lastIndex = filename.lastIndexOf('j');
				}
			}
			fileData.put(
						builder.toString() + '.' + (lastIndex == -1 ? "jpg" : filename.substring(lastIndex)),
						imageBytes
			);
			file.delete();
		}
		println("Files deleted and deduped. Recreating..");
		for (Map.Entry<String, byte[]> images : fileData.entrySet()) {
			File file = new File(directory, images.getKey());
			try {
				if (file.exists() && (!file.delete() || !file.createNewFile())) {
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			try (OutputStream stream = new FileOutputStream(file)) {
				stream.write(images.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		files = directory.listFiles();
		println("Created files!" + (files == null ? "" : " (" + files.length + ')'));

		fetcher.setDoneDuplicates(true);
	}

	@SneakyThrows
	private MessageDigest getDigest() {
		return MessageDigest.getInstance("SHA-1");
	}
}
