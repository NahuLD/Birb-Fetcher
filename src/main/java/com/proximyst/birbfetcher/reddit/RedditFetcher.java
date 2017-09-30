package com.proximyst.birbfetcher.reddit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proximyst.birbfetcher.reddit.api.RedditAPI;
import com.proximyst.birbfetcher.reddit.api.json.Post;
import com.proximyst.birbfetcher.reddit.api.retrofit.EnumStringConverterFactory;
import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import com.proximyst.birbfetcher.reddit.application.rest.GetImageById;
import com.proximyst.birbfetcher.reddit.application.rest.GetImageId;
import com.proximyst.birbfetcher.reddit.application.rest.GetImageJsonId;
import com.proximyst.birbfetcher.reddit.application.rest.GetRandomImage;
import com.proximyst.birbfetcher.reddit.threading.FetchThread;
import com.proximyst.birbfetcher.reddit.threading.PoolingThread;
import lombok.Getter;
import retrofit2.Retrofit;
import spark.Route;
import spark.Spark;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.proximyst.birbfetcher.reddit.Utilities.println;

@Getter
public class RedditFetcher {
	private final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://reddit.com")
				.addConverterFactory(new EnumStringConverterFactory())
				.build();
	private final RedditAPI redditApi = getRetrofit().create(RedditAPI.class);
	private final Gson gson = new GsonBuilder()
				.disableHtmlEscaping()
				.serializeNulls()
				.create();
	private final Queue<Post> posts = new LinkedBlockingQueue<>();
	private final File configurationFile = new File('.' + File.separator + "config.json");
	private Configuration configuration = null;
	private FilePool filePool;

	public static void main(String[] args) {
		new RedditFetcher().run();
	}

	private void run() {
		println("Starting to instantiate core elements.");

		if (configurationFile.exists()) {
			try (FileReader reader = new FileReader(configurationFile)) {
				configuration = gson.fromJson(reader, Configuration.class);
			} catch (IOException e) {
				println("Couldn't read config.");
				e.printStackTrace();

				if (configurationFile.exists()) { // might have changed, somehow.
					configurationFile.delete();
				}
			}
		}

		if (configuration == null) {
			configuration = new Configuration();
			configuration.setSubreddits(new ArrayList<>());
			configuration.getSubreddits().addAll(Arrays.asList("parrots", "birbs", "birb"));
			configuration.setThreads(16); // TODO: Find out if it's too much or too little. I think 8 or 4 might be enough.
			configuration.setBirbDirectory('.' + File.separator + "birbs");
			configuration.setPort(4500);
		}
		println("Instantiating pooling thread.");
		PoolingThread poolingThread = new PoolingThread(configuration, filePool);
		poolingThread.setName("Pooler");
		poolingThread.setDaemon(false);
		println("Starting pooling thread:", poolingThread.getName(), '(' + poolingThread.getId() + ')');
		poolingThread.start();
		println("Instantiating file pool.");
		filePool = new FilePool(poolingThread);
		// TODO: Thread for duplicates and converting incorrectly named files.

		FetchThread fetcherThread = new FetchThread(this);
		fetcherThread.setName("Fetcher");
		fetcherThread.setDaemon(false);
		println("Starting fetcher thread:", fetcherThread.getName(), '(' + fetcherThread.getId() + ')');
		fetcherThread.start();

		println("Setting Spark port to", configuration.getPort());
		Spark.port(configuration.getPort());

		println("Setting Spark paths.");
		final Route randomImage = new GetRandomImage(filePool);
		// TODO: Allow SSL
		Spark.path("/id", () -> {
			Spark.get("/text", new GetImageId(filePool));
			Spark.get("/json", new GetImageJsonId(filePool));
		});
		Spark.path("/img", () -> {
			Spark.get("/random", randomImage);
			Spark.get("/:id", new GetImageById(configuration, gson.toJson(
						new HashMap<String, Object>() {{
							put("error", 400);
							put("message", "not specified id");
							put("endpoint", "/img/:id");
						}}
			)));
		});
		Spark.path("/", () -> {
			Spark.get("/robots.txt", (req, resp) -> "User-agent: *\nAllow: /*");
			Spark.notFound(randomImage);
		});

		println("Finished instantiating core elements.");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			fetcherThread.interrupt();
			fetcherThread.getSlaves().forEach(Thread::interrupt);
			poolingThread.interrupt();
			Spark.stop();
			try {
				configurationFile.createNewFile();
			} catch (IOException ignored) {
			}
			try (FileWriter writer = new FileWriter(configurationFile)) {
				gson.toJson(configuration, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}
}
