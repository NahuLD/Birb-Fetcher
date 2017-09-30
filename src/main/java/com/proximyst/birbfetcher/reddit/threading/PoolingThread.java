package com.proximyst.birbfetcher.reddit.threading;

import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PoolingThread extends Thread {
	private final Configuration config;
	private final FilePool pool;

	@Override
	public void run() {
	}
}
