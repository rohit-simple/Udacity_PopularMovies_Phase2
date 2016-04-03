package com.app.rohit.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavoriteMoviesDatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "rohit_movies.db";
    private static final Integer DATABASE_VERSION = 1;


    public FavoriteMoviesDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String WHITESPACE = " ";
        final String COMMA = ",";
        final String TYPE_TEXT = "TEXT";
        final String TYPE_INTEGER = "INTEGER";
        final String TYPE_REAL = "REAL";

        final String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE" + WHITESPACE + FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES
                + "(" + FavoriteMoviesContract.FavoriteMovieEntry._ID + WHITESPACE + TYPE_INTEGER + WHITESPACE + "PRIMARY KEY" + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TITLE + WHITESPACE + TYPE_TEXT + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE + WHITESPACE + TYPE_TEXT + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RATING + WHITESPACE + TYPE_REAL + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_OVERVIEW + WHITESPACE + TYPE_TEXT +COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH + WHITESPACE + TYPE_TEXT + WHITESPACE + "NOT NULL" + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TRAILERS_JSON + WHITESPACE + TYPE_TEXT + COMMA +
                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_REVIEWS_JSON + WHITESPACE + TYPE_TEXT + ")";

        Log.v("rohit", SQL_CREATE_FAVORITE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES);
        onCreate(sqLiteDatabase);
    }
}
