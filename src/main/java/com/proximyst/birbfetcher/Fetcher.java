package com.proximyst.birbfetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proximyst.birbfetcher.api.RedditAPI;
import com.proximyst.birbfetcher.api.post.PostType;
import com.proximyst.birbfetcher.data.Configuration;
import com.proximyst.birbfetcher.gson.PostTypeAdapter;
import com.proximyst.birbfetcher.restful.image.Base64ById;
import com.proximyst.birbfetcher.restful.image.ImageById;
import com.proximyst.birbfetcher.restful.random.RandomBase64;
import com.proximyst.birbfetcher.restful.random.RandomImage;
import com.proximyst.birbfetcher.restful.random.RandomJsonPath;
import com.proximyst.birbfetcher.restful.random.RandomTextPath;
import com.proximyst.birbfetcher.retrofit.PostTypeConverter;
import com.proximyst.birbfetcher.threading.FileQueueThread;
import com.proximyst.birbfetcher.threading.FileVerificationThread;
import com.proximyst.birbfetcher.threading.PostFetchingThread;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import spark.Spark;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.proximyst.birbfetcher.Utilities.println;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Fetcher {
	private static final File configFile = new File('.' + File.separator + "config.json");
	private static final File blacklistFile = new File('.' + File.separator + "blacklist.json");
	private Set<String> blacklist = new HashSet<>();
	private Gson gson;
	private Retrofit retrofit;
	private RedditAPI redditApi;
	private Configuration config = null;
	private Timer timer;
	private FileVerificationThread fileVerificationThread;
	private FileQueueThread fileQueueThread;
	private PostFetchingThread fetchingThread;

	private void run() {
		println("Instantiating core components... (Gson, Retrofit, RedditAPI)");
		gson = new GsonBuilder()
					.serializeNulls()
					.serializeSpecialFloatingPointValues()
					.setPrettyPrinting()
					.registerTypeAdapter(
								PostType.class,
								new PostTypeAdapter()
					)
					.create();
		retrofit = new Retrofit.Builder()
					.addConverterFactory(new PostTypeConverter())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.baseUrl("https://reddit.com")
					.build();
		redditApi = retrofit.create(RedditAPI.class);
		println("Done instantiating core!");

		println("Reading/instantiating configuration...");
		if (configFile.exists()) {
			try (Reader reader = new FileReader(configFile)) {
				config = gson.fromJson(
							reader,
							Configuration.class
				);
			} catch (IOException e) {
				config = null;
				println("Couldn't read config!");
				e.printStackTrace();
				if (!configFile.delete()) {
					println("Couldn't delete corrupt config!");
				}
			}
		}
		if (config == null) {
			config = new Configuration();
			config.setSubreddits(Arrays.asList(
						"parrots",
						"birb",
						"birbs"
			));
			config.setThreads(16);
			config.setBirbDirectory('.' + File.separator + "birbs");
			config.setPort(4500);
			config.setUseJks(false);
			config.setJksName("keystore.jks");
			config.setJksPass("password");
			config.setTrustName("truststore.jks");
			config.setTrustPass("trustpassword");
			saveConfig();
		}
		println("Finished getting config!");

		println("Reading/instantiating blacklist...");
		if (blacklistFile.exists()) {
			try (Reader reader = new FileReader(blacklistFile)) {
				@SuppressWarnings("unchecked")
				List<String> blacklisted = gson.fromJson(
							reader,
							ArrayList.class
				);
				blacklist = new HashSet<>(blacklisted);
			} catch (IOException e) {
				println("Couldn't read blacklist!");
				e.printStackTrace();
				if (!blacklistFile.delete()) {
					println("Couldn't delete corrupt blacklist!");
				}
			}
		}
		println("Done getting blacklist!");

		println("Instantiating and starting threads...");
		timer = new Timer();
		fileVerificationThread = new FileVerificationThread(this);
		fileQueueThread = new FileQueueThread(this);
		fetchingThread = new PostFetchingThread(this);
		timer.scheduleAtFixedRate(
					fileVerificationThread,
					0,
					TimeUnit.MINUTES.toMillis(15)
		);
		timer.scheduleAtFixedRate(
					fileQueueThread,
					0,
					TimeUnit.SECONDS.toMillis(5)
		);
		timer.scheduleAtFixedRate(
					fetchingThread,
					0,
					TimeUnit.MINUTES.toMillis(10)
		);
		println("Finished instantiating and starting threads!");

		println("Instantiating and pathing the Spark RESTful API...");
		{
			if (config.isUseJks()) {
				Spark.secure(
							config.getJksName(),
							config.getJksPass(),
							config.getTrustName(),
							config.getTrustPass()
				);
			}

			Spark.port(config.getPort());
			final RandomBase64 randomBase64 = new RandomBase64(fileQueueThread);
			final RandomImage randomImage = new RandomImage(fileQueueThread);
			final RandomTextPath randomTextPath = new RandomTextPath(fileQueueThread);
			final RandomJsonPath randomJsonPath = new RandomJsonPath(
						this,
						fileQueueThread
			);
			final Base64ById base64ById = new Base64ById(config);
			final ImageById imageById = new ImageById(config);
			Spark.path(
						"/",
						() -> {
							Spark.get(
										"/",
										randomImage
							);
							Spark.get(
										"/robots.txt",
										(req, resp) -> ""
							);
							Spark.redirect.get(
										"/favicon.ico",
										"/random/image"
							);
						}
			);
			Spark.path(
						"/random",
						() -> {
							Spark.get(
										"/base64",
										randomBase64
							);
							Spark.get(
										"/image",
										randomImage
							);
							Spark.redirect.get(
										"/path",
										"/random/path/json"
							);
							Spark.get(
										"/path/json",
										randomJsonPath
							);
							Spark.get(
										"/path/text",
										randomTextPath
							);
						}
			);
			Spark.path(
						"/image",
						() -> {
							Spark.redirect.get(
										"/random",
										"/random/image"
							);
							Spark.get(
										"/:id",
										imageById
							);
							Spark.get(
										"/:id/image",
										imageById
							);
							Spark.get(
										"/:id/base64",
										base64ById
							);
						}
			);
			Spark.notFound(randomImage);
			Spark.internalServerError(randomImage);
		}
		println("Finished instantiating and pathing the Spark RESTful API!");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			timer.cancel();
			fetchingThread.getSlaves().forEach(Thread::interrupt);
			configFile.delete();
			try (Writer writer = new FileWriter(configFile)) {
				gson.toJson(
							config,
							writer
				);
			} catch (IOException e) {
				println("Couldn't save config!");
				e.printStackTrace();
			}
			blacklistFile.delete();
			try (Writer writer = new FileWriter(blacklistFile)) {
				gson.toJson(
							new ArrayList<>(blacklist),
							writer
				);
			} catch (IOException e) {
				println("Couldn't save blacklist!");
				e.printStackTrace();
			}
			Spark.stop();

			println("Shutting down!");
		}));
	}

	private void saveConfig() {
		if (configFile.exists()) {
			configFile.delete();
		}
		try {
			if (!configFile.createNewFile()) {
				throw new IOException("Couldn't create file \"" + configFile.getName() + "\"!");
			}
		} catch (IOException e) {
			println("Couldn't make new file!");
			e.printStackTrace();
			return;
		}
		try (Writer writer = new FileWriter(configFile)) {
			gson.toJson(
						config,
						writer
			);
		} catch (IOException e) {
			println("Couldn't write to config file!");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Fetcher().run();
	}
}
