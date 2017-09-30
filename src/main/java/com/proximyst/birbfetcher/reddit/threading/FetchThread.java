package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.RedditFetcher;
import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FetchThread extends Thread {
	private final RedditFetcher fetcher;
	@Getter private final Queue<Post> birbPosts = new LinkedBlockingQueue<>();
	@Getter private final Queue<ProcessingThread> slaves = new LinkedBlockingQueue<>();

	@Override
	public void run() {
		// TODO: Fetch from Reddit subs.

		if (birbPosts.peek() != null) {
			for (int i = 0; i < fetcher.getConfiguration().getThreads();) {
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
