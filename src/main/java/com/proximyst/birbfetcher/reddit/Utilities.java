package com.proximyst.birbfetcher.reddit;

import java.util.Arrays;
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
}
