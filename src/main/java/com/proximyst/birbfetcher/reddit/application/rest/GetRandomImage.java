package com.proximyst.birbfetcher.reddit.application.rest;

import com.proximyst.birbfetcher.reddit.Utilities;
import com.proximyst.birbfetcher.reddit.application.FilePool;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;

@RequiredArgsConstructor
public class GetRandomImage
			implements Route {
	private final FilePool pool;

	@Override
	public Object handle(Request request, Response response) {
		File file = pool.poll();
		if (file == null) {
			return "No image polled";
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
