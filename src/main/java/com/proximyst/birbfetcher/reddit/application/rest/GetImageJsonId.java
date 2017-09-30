package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;

@RequiredArgsConstructor
public class GetImageJsonId
			implements Route {
	private final FilePool pool;

	@Override
	public Object handle(Request request, Response response) {
		response.header("Content-Type", "text/plain; charset=utf-8");
		File file = pool.poll();
		String name = file == null ? "null" : '"' + file.getName() + '"';
		return "{\"id\":" + name + '}';
	}
}
