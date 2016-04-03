package com.app.rohit.popularmovies.main;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class MyApplication extends Application {
    private final String LOG_TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        //creating singleton instance for Picasso so that all of their network calls are intercepted by Stetho
        OkHttpClient picassoOkHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(picassoOkHttpClient))
                .build();
        Picasso.setSingletonInstance(picasso);
    }
}
