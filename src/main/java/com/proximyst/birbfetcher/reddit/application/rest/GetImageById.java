package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.application.Configuration;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
		File id = new File(directory, request.params(":id"));
		ByteOutputStream stream = new ByteOutputStream();
		try(FileInputStream reader = new FileInputStream(id)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = reader.read(buffer)) > 0) {
				stream.write(buffer, 0, length);
			}
		} catch (IOException e) {
			Utilities.println("Couldn't read file \"" + id + "\"!");
			e.printStackTrace();
			return null;
		}
		return stream.getBytes();
	}
}
