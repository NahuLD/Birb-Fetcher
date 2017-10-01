package com.proximyst.birbfetcher.retrofit;

import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class PostTypeConverter
			extends Converter.Factory {
	@Override
	public Converter<?, String> stringConverter(
				Type type,
				Annotation[] annotations,
				Retrofit retrofit
	) {
		if (type instanceof Class && ((Class) type).isEnum()) {
			return value -> ((Enum) value).name().toLowerCase();
		}
		return null;
	}
}
