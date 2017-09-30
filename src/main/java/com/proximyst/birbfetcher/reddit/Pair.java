package com.proximyst.birbfetcher.reddit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<A, B> {
	private A a;
	private B b;
}
