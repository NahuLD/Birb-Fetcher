package com.proximyst.birbfetcher.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.proximyst.birbfetcher.api.post.PostType;

import java.io.IOException;

public class PostTypeAdapter
			extends TypeAdapter<PostType> {
	@Override
	public void write(
				JsonWriter out,
				PostType value
	) throws IOException {
		if (value == null) {
			out.name("type").nullValue();
		} else {
			out.name("type").value(value.name().toLowerCase());
		}
	}

	@Override
	public PostType read(JsonReader in) throws IOException {
		while (in.hasNext()) {
			String name = in.nextName();
			if (!name.equalsIgnoreCase("type")) {
				in.skipValue();
				continue;
			}
			JsonToken next = in.peek();
			if (next == JsonToken.NULL) {
				return PostType.NEW;
			}
			if (next == JsonToken.NUMBER) {
				return in.nextString().startsWith("0") ? PostType.HOT : PostType.NEW;
			}
			if (next == JsonToken.BOOLEAN) {
				return in.nextBoolean() ? PostType.NEW : PostType.HOT;
			}
			return in.nextString().startsWith("0") || in.nextString().toLowerCase().startsWith("h")
						 ? PostType.HOT
						 : PostType.NEW;
		}
		return PostType.NEW;
	}
}
