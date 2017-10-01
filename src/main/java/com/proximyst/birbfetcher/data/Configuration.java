package com.proximyst.birbfetcher.data;

import lombok.Data;

import java.util.List;

@Data
public class Configuration {
	private List<String> subreddits;
	private int threads;
	private String birbDirectory;
	private int port;
	private boolean useJks;
	private String jksName;
	private String jksPass;
	private String trustName;
	private String trustPass;
}
