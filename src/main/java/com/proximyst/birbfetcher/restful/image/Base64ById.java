package com.proximyst.birbfetcher.restful.image;

import com.proximyst.birbfetcher.Utilities;
import com.proximyst.birbfetcher.data.Configuration;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.util.Base64;
import java.util.Optional;

@RequiredArgsConstructor
public class Base64ById
			implements Route {
	private final Configuration config;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		String id = request.params(":id");
		if (id == null || id.equals("")) {
			response.status(400);
			return null;
		}
		Optional<String> type = Utilities.getSafeContentType(id);
		if (!type.isPresent()) {
			response.status(404);
			return null;
		}
		File directory = new File(config.getBirbDirectory());
		if (!directory.isDirectory()) {
			response.status(500);
			return null;
		}
		File file = new File(
					directory,
					id
		);
		if (!file.exists()) {
			response.status(404);
			return null;
		}
		response.type("text/plain");
		return new String(Base64.getEncoder().encode(Utilities.readFile(file)));
	}
}
