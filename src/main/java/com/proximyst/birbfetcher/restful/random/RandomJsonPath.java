package com.proximyst.birbfetcher.restful.random;

import com.proximyst.birbfetcher.Fetcher;
import com.proximyst.birbfetcher.threading.FileQueueThread;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

@RequiredArgsConstructor
public class RandomJsonPath
			implements Route {
	private final Fetcher fetcher;
	private final FileQueueThread queue;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		response.type("application/json");
		return fetcher.getGson().toJson(new HashMap<String, String>() {{
			put(
						"file",
						queue.getQueue().getFile().getName()
			);
		}});
	}
}
