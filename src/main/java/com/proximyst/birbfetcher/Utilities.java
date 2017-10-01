package com.proximyst.birbfetcher;

import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum Utilities {
	;

	@SafeVarargs
	public static void println(final Object... components) {
		String toSend;
		if (components == null) {
			toSend = "null";
		} else {
			toSend = String.join(
						" ",
						Arrays.stream(components)
									.map(in -> in == null ? "null" : in.toString())
									.collect(Collectors.toList())
			);
		}
		String prefix = '[' + new GregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
					+ "] [" + Thread.currentThread().getName() + '-' + Thread.currentThread().getId() + "] ";
		System.out.println(prefix + toSend);
	}

	public static byte[] getSignature(final byte[] picture) {
		if (picture == null || picture.length <= 0) {
			return null;
		}
		return getDigest().digest(picture);
	}

	public static String getFileName(final byte[] signature) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < Math.min(
					64,
					signature.length
		); i++) {
			final byte digestedByte = signature[i];
			builder.append(Integer.toString(
						(digestedByte & 0xff) + 0x100,
						16
			).substring(1));
		}
		return builder.toString();
	}

	public static byte[] readFile(final File file) {
		try (InputStream stream = new FileInputStream(file)) {
			return readStream(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] readUrl(final String urlStr) {
		return readUrl(
					urlStr,
					conn -> {
					}
		);
	}

	public static byte[] readUrl(
				final String urlStr,
				final Consumer<URLConnection> connectionChanger
	) {
		try {
			URL url = new URL(urlStr);
			URLConnection connection = url.openConnection();
			connection.addRequestProperty(
						"User-Agent",
						"Mozilla/5.0"
			);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connectionChanger.accept(connection);
			connection.connect();
			try (InputStream stream = connection.getInputStream()) {
				return readStream(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] readStream(final InputStream inputStream) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			int length;
			byte[] buffer = new byte[1024];
			while ((length = inputStream.read(buffer)) >= 0) {
				stream.write(
							buffer,
							0,
							length
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream.toByteArray();
	}

	public static String getContentType(final File file) {
		return getContentType(file.getName());
	}

	public static Optional<String> getSafeContentType(final File file) {
		return getSafeContentType(file.getName());
	}

	public static Optional<String> getSafeContentType(final String name) {
		String ret = null;
		String extension = name;
		while (extension.charAt(0) == '.') {
			extension = extension.substring(1);
		}
		extension = extension.split(
					"[^a-z0-9A-Z]",
					2
		)[1];
		switch (extension.toLowerCase()) {
			case "png":
				ret = "image/png";
				break;
			case "jpg":
			case "jpeg":
				ret = "image/jpeg";
				break;
			case "gif":
			case "gifv":
				ret = "image/gif";
				break;
			case "svg":
				ret = "image/svg+xml";
				break;
			case "bmp":
				ret = "image/bmp";
				break;
			case "webp":
				ret = "image/webp";
				break;
			case "webm":
			case "mp4":
			case "mkv":
				ret = "video/webm";
				break;
		}
		return Optional.ofNullable(ret);
	}

	public static String getContentType(final String name) {
		return getSafeContentType(name).orElseThrow(() -> {
			String extension = name;
			while (extension.charAt(0) == '.') {
				extension = extension.substring(1);
			}
			extension = extension.split(
						"[^a-z0-9A-Z]",
						2
			)[1];
			return new IllegalArgumentException("Unsupported type! (" + extension + ')');
		});
	}

	@SneakyThrows(NoSuchAlgorithmException.class) // Shouldn't ever not include SHA-1
	public static MessageDigest getDigest() {
		return MessageDigest.getInstance("SHA-1");
	}
}
