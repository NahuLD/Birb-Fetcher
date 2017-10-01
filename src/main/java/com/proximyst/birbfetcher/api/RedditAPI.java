package com.proximyst.birbfetcher.api;

import com.proximyst.birbfetcher.api.post.PostType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditAPI {
	@GET("r/{subreddit}/{type}.json")
	Call<ResponseBody> getPosts(
				@Path("subreddit") String subreddit,
				@Path("type") PostType type
	);
}
