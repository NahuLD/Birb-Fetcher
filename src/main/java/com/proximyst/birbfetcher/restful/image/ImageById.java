package com.proximyst.birbfetcher.restful.image;

import com.proximyst.birbfetcher.Utilities;
import com.proximyst.birbfetcher.data.Configuration;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.util.Optional;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class ImageById
			implements Route {
	private final Configuration config;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		String id = request.params("id");
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
			println("Directory wasn't found!");
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
		response.type(type.get());
		return Utilities.readFile(file);
	}
}
