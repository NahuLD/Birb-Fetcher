package com.proximyst.birbfetcher.threading;

import com.proximyst.birbfetcher.Fetcher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class FileQueueThread
			extends Thread {
	private static final int capacity = 64; // Only 64 images will be 100% determined to be non dupe.
	@Getter private final FileQueue queue = new FileQueue();
	private final Fetcher fetcher;

	@Override
	public void run() {
		File directory = new File(fetcher.getConfig().getBirbDirectory());
		if (!directory.isDirectory()) {
			if (!rerun()) {
				run();
			}
			return;
		}
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			if (!rerun()) {
				run();
			}
			return;
		}
		while (queue.size() < capacity) {
			queue.add(files[ThreadLocalRandom.current().nextInt(files.length)]);
		}
		if (!rerun()) {
			run();
		}
	}

	private boolean rerun() {
		try {
			sleep(2000);
			return false;
		} catch (InterruptedException e) {
			println("Interrupted! Stopping thread...");
			return true;
		}
	}

	public class FileQueue {
		private final Set<File> files = new HashSet<>();
		private final Queue<File> fileQueue = new LinkedBlockingQueue<>();

		public boolean add(File file) {
			if (capacity <= files.size()) {
				return false;
			}
			if (files.add(file) && !fileQueue.offer(file)) {
				files.remove(file);
			}
			return files.contains(file);
		}

		public int size() {
			return files.size();
		}

		public synchronized File getFile() {
			while (files.size() <= 0) {
				// Wait for item.
			}
			File file = fileQueue.poll();
			if (file == null) {
				return getFile();
			}
			files.remove(file);
			if (file.exists()) {
				return file;
			}
			return getFile();
		}
	}
}
