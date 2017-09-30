package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.application.Configuration;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

@RequiredArgsConstructor
public class GetImageById implements Route {
	private final Configuration config;
	private final String error;

	@Override
	public Object handle(Request request, Response response) {
		if (request.params(":id") == null) {
			return error;
		}
		// TODO: Get image by supplied ID
		return null;
	}
}
