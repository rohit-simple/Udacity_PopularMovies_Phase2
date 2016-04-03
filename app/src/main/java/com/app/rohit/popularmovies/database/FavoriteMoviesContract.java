package com.app.rohit.popularmovies.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteMoviesContract {
    public static final String CONTENT_AUTHORITY = "com.app.rohit.popularmovies.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class FavoriteMovieEntry implements BaseColumns{
        public static final String TABLE_FAVORITE_MOVIES = "favorite_movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_TRAILERS_JSON = "trailers_json";
        public static final String COLUMN_REVIEWS_JSON = "reviews_json";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_FAVORITE_MOVIES).build();

        public static Uri buildFavoriteMoviesWithMovieIdUri(long movieId){
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }
    }
}
