package com.proximyst.birbfetcher.reddit.application;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor
public class FilePool {
	private final Queue<String> internalQueue = new LinkedBlockingQueue<>();
	private final Configuration config;

	public File poll() {
		return null;
	}
}
