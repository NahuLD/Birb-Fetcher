package com.proximyst.birbfetcher.reddit.api.retrofit;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class SortConverterFactory
			extends Converter.Factory {
	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(
				Type type,
				Annotation[] annotations,
				Retrofit retrofit
	) {
		return new SortResponseConverter();
	}

	@Override
	public Converter<?, RequestBody> requestBodyConverter(
				Type type,
				Annotation[] parameterAnnotations,
				Annotation[] methodAnnotations,
				Retrofit retrofit
	) {
		return new SortRequestConverter();
	}
}
