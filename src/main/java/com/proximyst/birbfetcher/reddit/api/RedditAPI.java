package com.proximyst.birbfetcher.reddit.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditAPI {
	@GET("r/{subreddit}/new.json")
	Call<ResponseBody> newPosts(@Path("subreddit") String subreddit, @Query("sort") String sorting);

	@GET("r/{subreddit}/hot.json")
	Call<ResponseBody> hotPosts(@Path("subreddit") String subreddit);
}
