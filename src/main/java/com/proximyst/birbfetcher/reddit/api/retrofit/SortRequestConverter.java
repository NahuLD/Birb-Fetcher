package com.proximyst.birbfetcher.reddit.api.retrofit;

import com.proximyst.birbfetcher.reddit.api.Sort;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

public class SortRequestConverter
			implements Converter<Sort, RequestBody> {
	private static final MediaType media = MediaType.parse("text/plain; charset=UTF-8");

	@Override
	public RequestBody convert(Sort value) {
		return RequestBody.create(media, value.name());
	}
}
