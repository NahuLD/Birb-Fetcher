package com.proximyst.birbfetcher.reddit.api.json;

import lombok.Data;

@Data
public class NewJson {
	private final String kind;
	private final NewData data;
}
