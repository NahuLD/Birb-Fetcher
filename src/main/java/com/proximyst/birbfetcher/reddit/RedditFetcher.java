package com.proximyst.birbfetcher.reddit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proximyst.birbfetcher.reddit.api.RedditAPI;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import com.proximyst.birbfetcher.reddit.api.retrofit.SortConverterFactory;
import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.proximyst.birbfetcher.reddit.threading.FetchThread;
import lombok.Getter;
import retrofit2.Retrofit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class RedditFetcher {
	private final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://reddit.com")
				.addConverterFactory(new SortConverterFactory())
				.build();
	private final RedditAPI redditApi = getRetrofit().create(RedditAPI.class);
	private final Gson gson = new GsonBuilder()
				.disableHtmlEscaping()
				.serializeNulls()
				.create();
	private final Queue<Post> posts = new LinkedBlockingQueue<>();
	private final File configurationFile = new File('.' + File.separatorChar + "config.json");
	private Configuration configuration = null;

	public static void main(String[] args) {
		new RedditFetcher().run();
	}

	private void run() {
		Utilities.println("Starting to instantiate core elements.");

		if (configurationFile.exists()) {
			try (FileReader reader = new FileReader(configurationFile)) {
				configuration = gson.fromJson(reader, Configuration.class);
			} catch (IOException e) {
				Utilities.println("Couldn't read config.");
				e.printStackTrace();

				if (configurationFile.exists()) { // might have changed, somehow.
					configurationFile.delete();
				}
			}
		}

		if (configuration == null) {
			configuration = new Configuration();
			configuration.getSubreddits().addAll(Arrays.asList("parrots", "birbs", "birb"));
			configuration.setThreads(16); // TODO: Find out if it's too much or too little. I think 8 or 4 might be enough.
		}

		FetchThread fetcherThread = new FetchThread(this);
		fetcherThread.setName("Fetcher");
		fetcherThread.setDaemon(false);
		Utilities.println("Starting fetcher thread:", fetcherThread.getName(), '(' + fetcherThread.getId() + ')');
		fetcherThread.start();
	}
}
