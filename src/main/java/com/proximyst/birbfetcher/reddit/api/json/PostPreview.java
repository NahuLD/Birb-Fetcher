package com.proximyst.birbfetcher.reddit.api.json;

import lombok.Data;

import java.util.List;

@Data
public class PostPreview {
	private final List<PostImage> images;
	private final boolean enabled;
}
