package com.proximyst.birbfetcher.api.post;

import lombok.Data;

import java.util.List;

@Data
public class PostsJson {
	private final PostsData data;

	@Data
	public static final class PostsData {
		private final List<Post> children;
	}
}
