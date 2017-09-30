package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

@RequiredArgsConstructor
public class GetImageId implements Route {
	private final FilePool pool;

	@Override
	public Object handle(Request request, Response response) {
		return pool.poll().getName();
	}
}
