package com.proximyst.birbfetcher.reddit.api.json;

import lombok.Data;

@Data
public class PostData {
	private final String domain;
	private final String approved_at_utc;
	private final String banned_by;
	private final int thumbnail_width;
	private final String subreddit;
	private final String selftext_html;
	private final String selftext;
	private final int likes;
	private final String suggested_sort;
	private final int view_count;
	private final String title;
	private final int score;
	private final boolean hidden;
	private final PostPreview preview;
	private final String post_hint;
	private final String permalink;
	private final String subreddit_type;
	private final boolean hide_score;
	private final String url;
	private final boolean quarantine;
}
