package com.app.rohit.popularmovies.retrofit;

import com.app.rohit.popularmovies.BuildConfig;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

public class RetrofitDownloadRestAdapter{
    private RetrofitDownloadInterface retrofitDownloadInterface;
    private final String API_KEY = BuildConfig.THEMOVIEDB_API_KEY;

    public RetrofitDownloadRestAdapter(){
        //StethoInspector is added to client so that Stetho could intercept all network calls made by retorfit
        //this way of creating OkHttpClient is needed as we are using retrofit:2.0.0-beta4
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        retrofitDownloadInterface = retrofit.create(RetrofitDownloadInterface.class);
    }

    public Call<DiscoverMovieData> discoverMovies(String sortBy){
        return retrofitDownloadInterface.discoverMovies(sortBy, API_KEY);
    }

    public Call<TrailerData> getTrailers(Long movieId){
        return retrofitDownloadInterface.getTrailers(movieId, API_KEY);
    }

    public Call<ReviewData> getReviews(Long movieId){
        return retrofitDownloadInterface.getReviews(movieId, API_KEY);
    }
}
