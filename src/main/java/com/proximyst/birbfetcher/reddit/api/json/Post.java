package com.proximyst.birbfetcher.reddit.api.json;

import lombok.Data;

@Data
public class Post {
	private final String kind;
	private final PostData data;
}
