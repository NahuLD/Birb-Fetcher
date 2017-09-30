package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.api.Sort;
import com.proximyst.birbfetcher.reddit.api.json.NewJson;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FetchThread
			extends Thread {
	private final RedditFetcher fetcher;
	@Getter private final Queue<Post> birbPosts = new LinkedBlockingQueue<>();
	@Getter private final Set<ProcessingThread> slaves = new HashSet<>();
	@Getter @Setter private Map<String, String> files = new HashMap<>();

	@Override
	public void run() {
		for (String subreddit : fetcher.getConfiguration().getSubreddits()) {
			try {
				// TODO: Do both new RISING and NEW Sorts to have more posts and be able to poll highest liked.
				String json = fetcher.getRedditApi().newPosts(subreddit, Sort.RISING).clone().execute().body();
				NewJson newPosts = fetcher.getGson().fromJson(json, NewJson.class);
				// Safety in case Reddit returns null for some reason.
				if (newPosts.getData() != null
							&& newPosts.getData().getChildren() != null) {
					newPosts.getData().getChildren().parallelStream().forEach(birbPosts::offer);
				}
			} catch (IOException e) {
				Utilities.println("Couldn't read reddit API for subreddit \"" + subreddit + "\"!");
				e.printStackTrace();
			}
		}

		if (birbPosts.peek() != null) {
			for (int i = 0; i < fetcher.getConfiguration().getThreads(); ) {
				ProcessingThread thread = new ProcessingThread(fetcher, this);
				thread.setName("ProcessingSlave-" + thread.getId());
				thread.setDaemon(false);
				if (slaves.add(thread)) {
					i++; // Only increase when threads have been properly made.
					thread.start();
				}
			}
		}

		try {
			sleep(TimeUnit.MINUTES.toMillis(5));
		} catch (InterruptedException ignored) {
			Utilities.println("Interrupted! Stopping thread...");
			return;
		}
		run();
	}
}
