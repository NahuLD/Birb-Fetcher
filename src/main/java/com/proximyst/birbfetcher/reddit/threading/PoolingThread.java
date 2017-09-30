package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.proximyst.birbfetcher.reddit.Utilities.println;

@RequiredArgsConstructor
public class PoolingThread
			extends Thread {
	private static final int capacity = 128;

	private final Configuration config;
	private final FilePool pool;
	@Getter private final Set<File> fileQueue = new LinkedHashSet<>(capacity, 0);

	@Override
	public void run() {
		int size = fileQueue.size();
//		if (size >= capacity) {
//			Iterator<File> iter = fileQueue.iterator();
//			for (int i = 0; i < Math.ceil(size / 10); i++) {
//				iter.remove();
//				iter.next();
//			}
//		}
		File directory = new File(config.getBirbDirectory());
		File[] children = directory.listFiles();
		if (children != null) {
			for (int i = 0; i < (capacity - size); i++) {
				while (!fileQueue.add(children[ThreadLocalRandom.current().nextInt(children.length)])
							&& (children.length >= capacity)) {
					// wait for a file to be added.
				}
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
