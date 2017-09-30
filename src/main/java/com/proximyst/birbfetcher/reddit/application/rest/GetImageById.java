package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.application.Configuration;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;

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
		File file = new File(directory, filename);
		if (!file.exists()) {
			return "No image found";
		}
		response.type(Utilities.getContentType(file));
		response.status(200);
		try(OutputStream outputStream = response.raw().getOutputStream();
				InputStream input = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) >= 0) {
				outputStream.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
