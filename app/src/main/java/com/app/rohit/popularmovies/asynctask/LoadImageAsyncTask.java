package com.app.rohit.popularmovies.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

public class LoadImageAsyncTask extends AsyncTask<Void, Void, Bitmap>{
    private static final String LOG_TAG = LoadImageAsyncTask.class.getSimpleName();

    public interface LoadImageAsyncTaskReporter{
        void sendingBitmap(Bitmap bitmap, Object object);
    }

    private String posterPath;
    private Object throwData;
    private LoadImageAsyncTaskReporter reporter;

    public LoadImageAsyncTask(Fragment fragment, String posterPath, Object throwData){
        this.posterPath = posterPath;
        try{
            this.reporter = (LoadImageAsyncTaskReporter) fragment;
        }catch(ClassCastException e){
            throw new ClassCastException("fragment must implement " + LoadImageAsyncTaskReporter.class.getSimpleName());
        }
        this.throwData = throwData;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        if(posterPath == null || posterPath.length() == 0){
            Log.e(LOG_TAG, "doInBackground() -> poster path is null or empty");
            return null;
        }
        return BitmapFactory.decodeFile(posterPath);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(reporter != null){
            reporter.sendingBitmap(bitmap, throwData);
        }
    }
}
