package com.proximyst.birbfetcher.reddit.api.retrofit;

import com.google.gson.annotations.SerializedName;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class EnumStringConverterFactory
			extends Converter.Factory {
	@Override
	public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (!(type instanceof Class) || !((Class) type).isEnum()) {
			return null;
		}
		return in -> {
			try {
				SerializedName annotation = in.getClass().getField(((Enum) in).name()).getAnnotation(SerializedName.class);
				if (annotation != null) {
					return annotation.value();
				}
			} catch (NoSuchFieldException ignored) {
			}
			return ((Enum) in).name();
		};
	}
}
