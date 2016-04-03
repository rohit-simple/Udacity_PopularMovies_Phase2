package com.app.rohit.popularmovies.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class SaveImageAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String LOG_TAG = SaveImageAsyncTask.class.getSimpleName();

    private Context context;
    private long movieId;
    private Bitmap bitmap;
    private SaveImageAsyncTaskReporter reporter;

    public interface SaveImageAsyncTaskReporter{
        void updateDatabasePosterPath(String path);
    }

    public SaveImageAsyncTask(Fragment fragment, Long movieId, Bitmap bitmap) {
        this.context = fragment.getActivity();
        this.movieId = movieId;
        this.bitmap = bitmap;
        try{
            reporter = (SaveImageAsyncTaskReporter) fragment;
        }catch(ClassCastException e){
            throw new ClassCastException("fragment must implement " + SaveImageAsyncTaskReporter.class.getSimpleName());
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        File internalStorage = context.getDir("MOVIE_POSTERS", Context.MODE_PRIVATE);
        File moviePosterPath = new File(internalStorage, movieId + ".png");
        try {
            if(moviePosterPath.exists()){
                moviePosterPath.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(moviePosterPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
            return moviePosterPath.toString();
        } catch (Exception e){
            Log.e(LOG_TAG, "doInBackground() -> " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String posterPath) {
        super.onPostExecute(posterPath);
        reporter.updateDatabasePosterPath(posterPath);
    }
}
