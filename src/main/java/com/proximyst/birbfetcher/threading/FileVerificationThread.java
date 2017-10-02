package com.proximyst.birbfetcher.threading;

import com.proximyst.birbfetcher.Fetcher;
import com.proximyst.birbfetcher.Utilities;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.proximyst.birbfetcher.Utilities.println;

@RequiredArgsConstructor
public class FileVerificationThread
			extends Thread {
	private final Fetcher fetcher;

	@Override
	public void run() {
		File directory = new File(fetcher.getConfig().getBirbDirectory());
		if (!directory.isDirectory()) {
			if (!rerun()) {
				run();
			}
			return;
		}
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			if (!rerun()) {
				run();
			}
			return;
		}
		Map<String, byte[]> fileData = new HashMap<>();
		for (File file : files) {
			if (isInterrupted()) {
				break; // Just write the files we know are verified
			}
			byte[] readFile = Utilities.readFile(file);
			if (readFile.length == 0) {
				continue;
			}
			if (!file.delete()) {
				println("Couldn't delete file \"" + file.getName() + "\"!");
				continue;
			}
			String extension = file.getName().substring(file.getName().indexOf('.'));
			while (extension.startsWith(".")) {
				extension = extension.substring(1);
			}
			fileData.put(
						Utilities.getFileName(Utilities.getSignature(readFile)) + '.' + extension,
						readFile
			);
		}

		fileData.forEach((name, data) -> {
			File file = new File(
						directory,
						name
			);
			try (OutputStream stream = new FileOutputStream(file)) {
				stream.write(data);
			} catch (IOException e) {
				println("Couldn't write to file \"" + name + "\"!");
				e.printStackTrace();
			}
		});
		println(
					"Verified",
					fileData.size(),
					"files. (Previously",
					files.length + ")"
		);
		if (!rerun()) {
			run();
		}
	}

	private boolean rerun() {
		try {
			sleep(TimeUnit.MINUTES.toMillis(15));
			return false;
		} catch (InterruptedException e) {
			println("Interrupted! Stopping thread...");
			return true;
		}
	}
}
