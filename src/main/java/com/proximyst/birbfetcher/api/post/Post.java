package com.proximyst.birbfetcher.api.post;

import lombok.Data;

import java.util.List;

@Data
public class Post {
	private final PostData data;
	
	@Data
	public static final class PostData {
		private final String banned_by;
		private final String subreddit;
		private final int likes;
		private final int view_count;
		private final String title;
		private final int score;
		private final boolean hidden;
		private final PostPreview preview;
		private final String post_hint;
		private final String permalink;
		private final String subreddit_type;
		private final boolean hide_score;
		private final boolean quarantine;
	}

	@Data
	public static final class PostPreview {
		private final List<PostImage> images;
	}

	@Data
	public static final class PostImage {
		private final ImageSource source;
	}

	@Data
	public static final class ImageSource {
		private final String url;
	}
}
