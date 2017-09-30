package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.proximyst.birbfetcher.reddit.Utilities.println;

@RequiredArgsConstructor
public class PoolingThread
			extends Thread {
	private static final int capacity = 128;

	private final Configuration config;
	private final FilePool pool;
	@Getter private final Queue<File> fileQueue = new LinkedBlockingQueue<>(capacity);

	@Override
	public void run() {
		int size = fileQueue.size();
		if (size >= capacity) {
			for (int i = 0; i < Math.ceil(size / 10); i++) {
				fileQueue.poll();
			}
		}
		File directory = new File(config.getBirbDirectory());
		File[] children = directory.listFiles();
		if (children != null) {
			for (int i = 0; i < (capacity - size); i++) {
				fileQueue.offer(children[ThreadLocalRandom.current().nextInt(children.length)]);
			}
		}
		try {
			sleep(TimeUnit.SECONDS.toMillis(2));
		} catch (InterruptedException e) {
			println("Interrupted! Stopping thread...");
			return;
		}
		run();
	}
}
