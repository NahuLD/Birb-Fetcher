package com.proximyst.birbfetcher.data;

import lombok.Data;

import java.util.List;

@Data
public class Configuration {
	private List<String> subreddits;
	private int threads;
	private String birbDirectory;
	private int port;
}
