package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.application.Configuration;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.util.Optional;

@RequiredArgsConstructor
public class GetImageById
			implements Route {
	private final Configuration config;
	private final String error;

	@Override
	public Object handle(Request request, Response response) {
		if (request.params(":id") == null) {
			return error;
		}
		File directory = new File(config.getBirbDirectory());
		if (!directory.isDirectory()) {
			return null;
		}
		String filename;
		{
			filename = request.params(":id");
			if (filename.lastIndexOf('/') != -1) {
				filename = filename.substring(filename.lastIndexOf('/'));
			}
		}
		File id = new File(directory, filename);
		final Optional<byte[]> bytes = Utilities.readImage(id);
		bytes.ifPresent(b -> response.header("Content-Type", Utilities.getContentType(id) + "; charset=utf-8"));
		return bytes.orElse(null);
	}
}
