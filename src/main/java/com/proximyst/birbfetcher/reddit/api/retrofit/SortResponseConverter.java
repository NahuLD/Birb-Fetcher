package com.proximyst.birbfetcher.reddit.api.retrofit;

import com.proximyst.birbfetcher.reddit.api.Sort;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

public class SortResponseConverter
			implements Converter<ResponseBody, Sort> {
	@Override
	public Sort convert(ResponseBody value) throws IOException {
		return value.string().toLowerCase().charAt(0) == 'n' ? Sort.NEW : Sort.RISING;
	}
}
