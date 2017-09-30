package com.proximyst.birbfetcher.reddit.application;

import com.proximyst.birbfetcher.reddit.threading.PoolingThread;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class FilePool {
	private final PoolingThread poolingThread;

	public File poll() {
		File file;
		do {
			file = poolingThread.getFileQueue().poll();
		} while (file == null);
		return file;
	}
}
