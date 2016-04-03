package com.app.rohit.popularmovies.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.retrofit.DiscoverMovieData;
import com.app.rohit.popularmovies.retrofit.RetrofitDownloadRestAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MoviesDownloadService extends Service {
    private final String LOG_TAG = MoviesDownloadService.class.getSimpleName();

    private LocalBroadcastManager localBroadcastManager;

    private static List<DiscoverMovieData.MovieData> movieDetailsList = null;
    private static Enums.SERVICE_STATUS serviceStatus = Enums.SERVICE_STATUS.NONE;
    private static Enums.SERVICE_DATA_STATUS serviceDataStatus = Enums.SERVICE_DATA_STATUS.NONE;

    public static Enums.SERVICE_STATUS getServiceStatus() {
        return serviceStatus;
    }

    public static Enums.SERVICE_DATA_STATUS getServiceDataStatus() {
        return serviceDataStatus;
    }

    public static List<DiscoverMovieData.MovieData> getMovieDetailsList() {
        if(movieDetailsList != null){
            return new ArrayList<>(movieDetailsList);   //passing clone, not reference
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendServiceStatusIntent(Enums.SERVICE_STATUS.STARTED);

        if (intent != null) {
            if (intent.hasExtra(getString(R.string.extra_settings_sort_by))) {
                final String sortMethod = intent.getStringExtra(getString(R.string.extra_settings_sort_by));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RetrofitDownloadRestAdapter retrofitDownloadRestAdapter = new RetrofitDownloadRestAdapter();
                        try {
                            Response<DiscoverMovieData> response = retrofitDownloadRestAdapter.discoverMovies(sortMethod).execute();
                            DiscoverMovieData discoverMovieData = response.body();
                            if (discoverMovieData != null) {
                                movieDetailsList = discoverMovieData.getResults();

                            /*
                                logic to remove movies where poster path is null
                                because we are just showing poster to user
                                and if there is no poster, user will get no information about what movie it is
                            */
                                int size = movieDetailsList.size();
                                for (int i = 0; i < size; i++) {
                                    DiscoverMovieData.MovieData movieData = movieDetailsList.get(i);
                                    if (movieData.getPoster_path() == null || movieData.getPoster_path().trim().equals("")) {
                                        movieDetailsList.remove(movieData);
                                        size--;
                                        i--;
                                    }
                                }
                            }

                            if (movieDetailsList == null) {
                                sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS.DATA_NULL_OR_EMPTY);
                            } else if (movieDetailsList.isEmpty()) {
                                sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS.DATA_NULL_OR_EMPTY);
                            } else {
                                sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS.DATA_FULL);
                            }
                        } catch (IOException exception) {
                            Log.e(LOG_TAG, "onStartCommand() -> " + exception.getMessage());
                            sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS.ERROR);
                        }
                        sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED);
                    }
                }).start();
            } else {
                Log.e(LOG_TAG, "onStartCommand() -> intent doesn't have sort_by key");
                sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS);
            }
        } else {
            Log.e(LOG_TAG, "onStartCommand() -> intent is null");
            sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS);
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendServiceStatusIntent(Enums.SERVICE_STATUS status) {
        serviceStatus = status;
        Intent intent = new Intent(getString(R.string.movies_service_broadcast_action));
        intent.putExtra(getString(R.string.extra_service_status), status);
        localBroadcastManager.sendBroadcast(intent);
        if (status == Enums.SERVICE_STATUS.FINISHED || status == Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS) {
            stopSelf();
        }
    }

    private void sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS status) {
        serviceDataStatus = status;
        Intent intent = new Intent(getString(R.string.movies_service_broadcast_action));
        intent.putExtra(getString(R.string.extra_service_data_status), status);
        if (status == Enums.SERVICE_DATA_STATUS.DATA_FULL) {
            intent.putParcelableArrayListExtra(getString(R.string.extra_service_data), new ArrayList<Parcelable>(movieDetailsList));
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void clear(){
        serviceStatus = Enums.SERVICE_STATUS.NONE;
        serviceDataStatus = Enums.SERVICE_DATA_STATUS.NONE;
        movieDetailsList = null;
    }
}
