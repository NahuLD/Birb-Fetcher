package com.proximyst.birbfetcher.reddit;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

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
		ByteOutputStream stream = new ByteOutputStream();
		try(FileInputStream reader = new FileInputStream(file)) {
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
		return Optional.ofNullable(stream.getBytes());
	}
}
