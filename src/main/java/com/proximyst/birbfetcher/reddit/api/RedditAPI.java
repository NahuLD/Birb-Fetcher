package com.proximyst.birbfetcher.reddit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditAPI {
	@GET("r/{subreddit}/new.json")
	Call<String> newPosts(@Path("subreddit") String subreddit, @Query("sort") Sort sorting);
}
