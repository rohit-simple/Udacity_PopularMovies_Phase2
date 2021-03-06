package com.app.rohit.popularmovies.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.retrofit.RetrofitDownloadRestAdapter;
import com.app.rohit.popularmovies.retrofit.ReviewData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ReviewsDownloadService extends Service {
    private final String LOG_TAG = ReviewsDownloadService.class.getSimpleName();

    private LocalBroadcastManager localBroadcastManager;

    private static List<ReviewData.Review> reviewsList = null;
    private static Enums.SERVICE_STATUS serviceStatus = Enums.SERVICE_STATUS.NONE;
    private static Enums.SERVICE_DATA_STATUS serviceDataStatus = Enums.SERVICE_DATA_STATUS.NONE;

    public static List<ReviewData.Review> getReviewsList() {
        if (reviewsList != null) {
            return new ArrayList<>(reviewsList);
        }
        return null;
    }

    public static Enums.SERVICE_STATUS getServiceStatus() {
        return serviceStatus;
    }

    public static Enums.SERVICE_DATA_STATUS getServiceDataStatus() {
        return serviceDataStatus;
    }

    @Override
    public IBinder onBind(Intent intent) {
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
            if (intent.hasExtra(getString(R.string.extra_movie_id))) {
                final Long movieId = intent.getLongExtra(getString(R.string.extra_movie_id), -1);
                if (movieId != -1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            RetrofitDownloadRestAdapter retrofitDownloadRestAdapter = new RetrofitDownloadRestAdapter();
                            try {
                                Response<ReviewData> response = retrofitDownloadRestAdapter.getReviews(movieId).execute();
                                ReviewData reviewData = response.body();
                                if (reviewData != null) {
                                    reviewsList = reviewData.getResults();

                                    int size = reviewsList.size();
                                    for (int i = 0; i < size; i++) {
                                        ReviewData.Review review = reviewsList.get(i);
                                        if (review.getAuthor() == null ||
                                                review.getAuthor().trim().equals("") ||
                                                review.getContent() == null ||
                                                review.getContent().trim().equals("")) {
                                            reviewsList.remove(review);
                                            size--;
                                            i--;
                                        }
                                    }
                                }

                                if (reviewsList == null) {
                                    sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS.DATA_NULL_OR_EMPTY);
                                } else if (reviewsList.isEmpty()) {
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
                    Log.e(LOG_TAG, "onStartCommand() -> movie id is -1");
                    sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS);
                }
            } else {
                Log.e(LOG_TAG, "onStartCommand() -> intent doesn't have movie id");
                sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS);
            }
        } else {
            Log.e(LOG_TAG, "onStartCommand() -> intent is null");
            sendServiceStatusIntent(Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS);
        }

        return Service.START_REDELIVER_INTENT;
    }

    private void sendServiceStatusIntent(Enums.SERVICE_STATUS status) {
        serviceStatus = status;
        Intent intent = new Intent(getString(R.string.reviews_service_broadcast_action));
        intent.putExtra(getString(R.string.extra_service_status), status);
        localBroadcastManager.sendBroadcast(intent);
        if (status == Enums.SERVICE_STATUS.FINISHED || status == Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS) {
            stopSelf();
        }
    }

    private void sendServiceDataStatusIntent(Enums.SERVICE_DATA_STATUS status) {
        serviceDataStatus = status;
        Intent intent = new Intent(getString(R.string.reviews_service_broadcast_action));
        intent.putExtra(getString(R.string.extra_service_data_status), status);
        if (status == Enums.SERVICE_DATA_STATUS.DATA_FULL) {
            intent.putParcelableArrayListExtra(getString(R.string.extra_service_data), new ArrayList<Parcelable>(reviewsList));
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void clear(){
        serviceStatus = Enums.SERVICE_STATUS.NONE;
        serviceDataStatus = Enums.SERVICE_DATA_STATUS.NONE;
        reviewsList = null;
    }
}
