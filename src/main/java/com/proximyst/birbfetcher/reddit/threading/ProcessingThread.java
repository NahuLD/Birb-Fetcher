package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class ProcessingThread
			extends Thread {
	private static final Map<String, Function<String, String>> urls = new HashMap<>();

	private final RedditFetcher fetcher;
	private final FetchThread master;

	static {
		urls.put("i.redd.it", (url) -> url);
		urls.put("i.imgur.com", (url) -> url);
		urls.put("imgur.com", (url) -> {
			String[] split = url.replaceFirst("https?://", "").split("/");
			if (split[1].equalsIgnoreCase("a")) {
				// TODO: Handle albums using imgur API
				// https://apidocs.imgur.com/
				return null;
			}
			return "https://i.imgur.com/" + split[1];
		});
	}

	@Override
	public void run() {
		Post post;
		while ((post = master.getBirbPosts().poll()) != null) {
			if (post.getData() == null) { // Shouldn't ever, but it may at some point.
				continue;
			}
			String domain = post.getData().getDomain();
			final Function<String, String> urlFunction = urls.get(domain);
			if (urlFunction == null) {
				continue; // Only allow certain URLs to save a few risks.
			}
			String url = urlFunction.apply(post.getData().getUrl());
			if (url == null) {
				continue; // An error must've occurred to return null, or it is unsupported.
			}

			// TODO: Do hashes on pictures and save them.
		}
	}
}
