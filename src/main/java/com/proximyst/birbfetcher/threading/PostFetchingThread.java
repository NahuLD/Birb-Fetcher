package com.proximyst.birbfetcher.threading;

import com.proximyst.birbfetcher.Fetcher;
import com.proximyst.birbfetcher.api.post.Post;
import com.proximyst.birbfetcher.api.post.PostType;
import com.proximyst.birbfetcher.api.post.PostsJson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class PostFetchingThread
			extends TimerTask {
	@Getter private final Queue<Post> postQueue = new LinkedBlockingQueue<>();
	@Getter private final Set<PostProcessingSlave> slaves = new HashSet<>();
	private final Fetcher fetcher;

	@Override
	public void run() {
		File directory = new File(fetcher.getConfig().getBirbDirectory());
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}

		fetcher.getConfig().getSubreddits().parallelStream().forEach(sub -> {
			for (PostType type : PostType.values()) {
				try {
					fetcher.getGson().fromJson(
								fetcher.getRedditApi().getPosts(
											sub,
											type
								).clone().execute().body().string(),
								PostsJson.class
					).getData().getChildren().forEach(postQueue::offer);
				} catch (IOException e) {
					println("Couldn't read subreddit \"" + sub + "\" for type \"" + type.name() + "\"!");
					e.printStackTrace();
				} catch (NullPointerException ignored) {
					println("Couldn't read subreddit \"" + sub + "\" for type \"" + type.name() + "\"! Didn't contain data.");
				}
			}
		});

		IntStream
					.range(
								0,
								fetcher.getConfig().getThreads()
					)
					.mapToObj(i -> new PostProcessingSlave(
								this,
								fetcher.getBlacklist(),
								directory
					))
					.filter(this::daemon)
					.peek(slaves::add)
					.forEach(Thread::start);
	}

	private final boolean daemon(Thread thread) {
		try {
			if (!thread.isAlive()) {
				thread.setDaemon(false);
				return true;
			}
			return false;
		} catch (SecurityException ignored) {
			return false;
		}
	}
}
