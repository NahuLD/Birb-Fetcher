package com.proximyst.birbfetcher.restful.random;

import com.proximyst.birbfetcher.Utilities;
import com.proximyst.birbfetcher.threading.FileQueueThread;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class RandomImage
			implements Route {
	private final FileQueueThread queue;

	@Override
	public Object handle(
				Request request,
				Response response
	) {
		File file = queue.getQueue().getFile();
		response.type(Utilities.getContentType(file));
		try (OutputStream outputStream = response.raw().getOutputStream()) {
			outputStream.write(Utilities.readFile(file));
		} catch (IOException e) {
			println("An error occurred while writing a random image!");
			e.printStackTrace();
		}
		return "";
	}
}
