package com.app.rohit.popularmovies.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitDownloadInterface {

    @GET("discover/movie")
    Call<DiscoverMovieData> discoverMovies(@Query("sort_by") String sortBy, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<TrailerData> getTrailers(@Path("id") Long movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewData> getReviews(@Path("id") Long movieId, @Query("api_key") String apiKey);
}
