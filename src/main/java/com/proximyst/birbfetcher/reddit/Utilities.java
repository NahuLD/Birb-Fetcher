package com.proximyst.birbfetcher.reddit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Utilities {
	;

	@SafeVarargs
	public static <T> void println(T... elements) {
		final String toPrint = String.join(
					" ",
					Arrays.stream(elements)
								.map(in -> in == null ? "null" : in.toString())
								.collect(Collectors.toList())
		);
		System.out.println('[' + Thread.currentThread().getName() + "] " + toPrint);
	}

	public static Optional<byte[]> readImage(File file) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (FileInputStream reader = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = reader.read(buffer)) > 0) {
				stream.write(buffer, 0, length);
			}
		} catch (IOException e) {
			Utilities.println("Couldn't read file \"" + file.getName() + "\"!");
			e.printStackTrace();
			return Optional.empty();
		}
		return Optional.ofNullable(stream.toByteArray());
	}

	public static String getContentType(File file) {
		return getContentType(file.getName().substring(file.getName().lastIndexOf('.')));
	}

	public static String getContentType(String name) {
		switch (name.toLowerCase()) {
			case "png":
				return "image/png";
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "gif":
			case "gifv":
				return "image/gif";
			case "svg":
				return "image/svg+xml";
			case "bmp":
				return "image/bmp";
			case "webp":
				return "image/webp";
			case "webm":
			case "mp4":
			case "mkv":
				return "video/webm";
			default:
				throw new IllegalArgumentException("Unsupported type!");
		}
	}
}
