package com.proximyst.birbfetcher.reddit.application;

import com.proximyst.birbfetcher.reddit.threading.PoolingThread;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Iterator;

@RequiredArgsConstructor
public class FilePool {
	private final PoolingThread poolingThread;

	public File poll() {
		File file = null;
		Iterator<File> iterator = poolingThread.getFileQueue().iterator();
		do {
			if (iterator.hasNext()) {
				file = iterator.next();
				iterator.remove();
			}
		} while (file == null);
		return file;
	}
}
