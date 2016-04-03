package com.app.rohit.popularmovies.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.io.File;

public class DeleteImageAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final String LOG_TAG = DeleteImageAsyncTask.class.getSimpleName();

    private Context context;
    private long movieId;
    private DeleteImageAsyncTaskReporter reporter;

    public interface DeleteImageAsyncTaskReporter{
        void updateImageDeletionStatus(Boolean status);
    }

    public DeleteImageAsyncTask(Fragment fragment, Long movieId){
        this.context = fragment.getActivity();
        this.movieId = movieId;
        try{
            reporter = (DeleteImageAsyncTaskReporter) fragment;
        }catch(ClassCastException e){
            throw new ClassCastException("fragment must implement " + DeleteImageAsyncTaskReporter.class.getSimpleName());
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        File internalStorage = context.getDir("MOVIE_POSTERS", Context.MODE_PRIVATE);
        File moviePosterPath = new File(internalStorage, movieId + ".png");
        if(moviePosterPath.exists()){
            return moviePosterPath.delete();
        }
        return true;    //in case movie poster doesn't exist, still deletion is successful in other sense
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        reporter.updateImageDeletionStatus(status);
    }
}
