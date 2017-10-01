package com.proximyst.birbfetcher.restful.random;

import com.proximyst.birbfetcher.threading.FileQueueThread;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

@RequiredArgsConstructor
public class RandomTextPath
			implements Route {
	private final FileQueueThread queueThread;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		response.type("text/plain");
		return queueThread.getQueue().getFile().getName();
	}
}
