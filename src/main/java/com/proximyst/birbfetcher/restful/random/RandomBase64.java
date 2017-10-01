package com.proximyst.birbfetcher.restful.random;

import com.proximyst.birbfetcher.Utilities;
import com.proximyst.birbfetcher.threading.FileQueueThread;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Base64;

@RequiredArgsConstructor
public class RandomBase64
			implements Route {
	private final FileQueueThread queue;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		response.type("text/plain");
		return new String(Base64.getEncoder().encode(Utilities.readFile(queue.getQueue().getFile())));
	}
}
