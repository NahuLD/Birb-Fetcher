package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;

@RequiredArgsConstructor
public class GetRandomImage
			implements Route {
	private final FilePool pool;

	@Override
	public Object handle(Request request, Response response) {
		File file = pool.poll();
		if (file != null) {
			response.header("Content-Type", Utilities.getContentType(file) + "; charset=utf-8");
		}
		return Utilities.readImage(file).orElse(null);
	}
}
