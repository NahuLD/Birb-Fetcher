package com.proximyst.birbfetcher.threading;

import com.proximyst.birbfetcher.Fetcher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

	public class FileQueue
				extends LinkedHashSet<File> {
		@Override
		public boolean add(File file) {
			if (capacity <= size()) {
				return false;
			}
			return super.add(file);
		}

		public synchronized File getFile() {
			while (size() <= 0) {
				// Wait for item.
			}
			Iterator<File> iter = iterator();
			if (!iter.hasNext()) {
				// Shouldn't ever be fired, but it may.
				return getFile();
			}
			File file = iter.next();
			if (!file.exists()) { // Might be queued but removed by the verification
				return getFile();
			}
			try {
				return file;
			} finally {
				iter.remove();
			}
		}
	}
}
