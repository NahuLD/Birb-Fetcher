package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.api.Sort;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FetchThread
			extends Thread {
	private final RedditFetcher fetcher;
	@Getter private final Queue<Post> birbPosts = new LinkedBlockingQueue<>();
	@Getter private final Queue<ProcessingThread> slaves = new LinkedBlockingQueue<>();

	@Override
	public void run() {
		// TODO: Fetch from Reddit subs.
		for (String subreddit : fetcher.getConfiguration().getSubreddits()) {
			try {
				// TODO: Do both new RISING and NEW Sorts to have more posts and be able to poll highest liked.
				String json = fetcher.getRedditApi().newPosts(subreddit, Sort.RISING).clone().execute().body();
			} catch (IOException e) {
				Utilities.println("Couldn't read reddit API for subreddit \"" + subreddit + "\"!");
				e.printStackTrace();
			}
		}

		if (birbPosts.peek() != null) {
			for (int i = 0; i < fetcher.getConfiguration().getThreads(); ) {
				ProcessingThread thread = new ProcessingThread(fetcher, this);
				thread.setName("ProcessingSlave-" + thread.getId());
				if (slaves.offer(thread)) {
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
