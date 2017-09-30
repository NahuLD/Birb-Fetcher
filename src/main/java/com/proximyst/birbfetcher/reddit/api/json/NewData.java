package com.proximyst.birbfetcher.reddit.api.json;

import lombok.Data;

import java.util.List;

@Data
public class NewData {
	private final String modhash;
	private final String whitelist_status;
	private final List<Post> children;
	private final String after;
	private final String before;
}
