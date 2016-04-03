package com.app.rohit.popularmovies.asynctask;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.database.FavoriteMoviesContract;

public class InsertDeleteInDatabaseAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String LOG_TAG = InsertDeleteInDatabaseAsyncTask.class.getSimpleName();

    private ContentValues contentValues;
    private Enums.DATABASE_OPERATION_TYPE operationType;
    private InsertDeleteInDatabaseAsyncTaskReporter reporter;
    private Context context;

    public InsertDeleteInDatabaseAsyncTask(Fragment fragment, Enums.DATABASE_OPERATION_TYPE operationType, ContentValues contentValues){
        this.operationType = operationType;
        this.contentValues = contentValues;
        this.context = fragment.getActivity();
        try{
            this.reporter = (InsertDeleteInDatabaseAsyncTaskReporter) fragment;
        }catch(ClassCastException e){
            throw new ClassCastException("fragment has to implement " + InsertDeleteInDatabaseAsyncTaskReporter.class.getSimpleName());
        }
    }

    public interface InsertDeleteInDatabaseAsyncTaskReporter{
        void updateInsertionResult(boolean status);
        void updateDeletionResult(boolean status);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Uri uri;
        switch(operationType){
            case INSERT_FAVORITE_MOVIE:
                try{
                    uri = context.getContentResolver().insert(FavoriteMoviesContract.FavoriteMovieEntry.CONTENT_URI, contentValues);
                    if(uri != null){
                        Log.d(LOG_TAG, "INSERT_FAVORITE_MOVIE result uri is " + uri);
                        return true;
                    }else{
                        Log.e(LOG_TAG, "INSERT_FAVORITE_MOVIE uri received is null" );
                        return false;
                    }
                }catch(SQLException e){
                    Log.e(LOG_TAG, e.getMessage());
                    return false;
                }


            case DELETE_FAVORITE_MOVIE:
                //deletion logic is twisted as the movie id will be passed in content values same as insertion so as to get a little similarity in both operations to group them in asynctask
                if(contentValues == null || !contentValues.containsKey(FavoriteMoviesContract.FavoriteMovieEntry._ID)){
                    Log.e(LOG_TAG, "DELETE_FAVORITE_MOVIE didn't get movie Id in content Values");
                }else{
                    long movieId = contentValues.getAsLong(FavoriteMoviesContract.FavoriteMovieEntry._ID);
                    uri = FavoriteMoviesContract.FavoriteMovieEntry.buildFavoriteMoviesWithMovieIdUri(movieId);
                    int result = context.getContentResolver().delete(uri, null, null);
                    if(result == 0){
                        Log.e(LOG_TAG, "this movie id doesn't exist");
                        return false;
                    }else{
                        Log.d(LOG_TAG, "DELETE_FAVORITE_MOVIE movie Id " + movieId + " related row is deleted");
                        return true;
                    }
                }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        switch(operationType){
            case INSERT_FAVORITE_MOVIE:
                reporter.updateInsertionResult(result);
                break;
            case DELETE_FAVORITE_MOVIE:
                reporter.updateDeletionResult(result);
        }
    }
}
