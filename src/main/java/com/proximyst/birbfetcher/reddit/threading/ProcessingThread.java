package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.api.json.ImageSource;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import com.proximyst.birbfetcher.reddit.api.json.PostData;
import com.proximyst.birbfetcher.reddit.api.json.PostImage;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ProcessingThread
			extends Thread {
	private final RedditFetcher fetcher;
	private final FetchThread master;

	@Override
	public void run() {
		Post post;
		while ((post = master.getBirbPosts().poll()) != null) {
			if (post.getData() == null) { // Shouldn't ever, but it may at some point.
				continue;
			}
			PostData data = post.getData();
			if (data.isHidden()
						|| data.isQuarantine()
						|| (data.getBanned_by() != null
						&& !data.getBanned_by().equals(""))
						|| !data.getPost_hint().equalsIgnoreCase("image")
						|| !data.getSubreddit_type().equalsIgnoreCase("public")) {
				continue;
			}

			Set<String> imageUrls = new HashSet<>();
			data.getPreview().getImages().stream()
					.map(PostImage::getSource)
					.map(ImageSource::getUrl)
					.forEach(imageUrls::add);

			// TODO: Do hashes on pictures and save them.
		}
	}
}
