package com.proximyst.birbfetcher.reddit.application;

import lombok.Data;

import java.util.List;

@Data
public class Configuration {
	private List<String> subreddits;
	private int threads;
}
