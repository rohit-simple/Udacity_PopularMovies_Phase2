package com.app.rohit.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class FavoriteMoviesContentProvider extends ContentProvider{
    private static final int FAVORITE_MOVIES = 100;
    private static final int FAVORITE_MOVIES_WITH_ID = 101;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private FavoriteMoviesDatabaseHelper favoriteMoviesDatabaseHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteMoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES, FAVORITE_MOVIES);
        uriMatcher.addURI(authority, FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES + "/#", FAVORITE_MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        favoriteMoviesDatabaseHelper = new FavoriteMoviesDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
        switch(URI_MATCHER.match(uri)){
            case FAVORITE_MOVIES:
                if(projections == null || projections.length != 2){
                    throw new UnsupportedOperationException("projections array length is not 2 for " + uri);
                }else{
                    if(!((projections[0].equals(FavoriteMoviesContract.FavoriteMovieEntry._ID) && projections[1].equals(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH)) ||
                            (projections[1].equals(FavoriteMoviesContract.FavoriteMovieEntry._ID) && projections[0].equals(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH)))){
                        throw new UnsupportedOperationException("these columns are not allowed for query. Only _ID and COLUMN_POSTER_PATH!");
                    }else if(selection != null || selectionArgs != null || sortOrder != null){
                        throw new UnsupportedOperationException("only projections are allowed for " + uri);
                    }else{
                        final SQLiteDatabase database = favoriteMoviesDatabaseHelper.getReadableDatabase();
                        return database.query(FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES, projections, null, null, null, null, null);
                    }
                }
            case FAVORITE_MOVIES_WITH_ID:
                if(selection != null && selectionArgs != null && sortOrder != null){
                    throw new UnsupportedOperationException("only projections are allowed for " + uri);
                }else{
                    final SQLiteDatabase database = favoriteMoviesDatabaseHelper.getReadableDatabase();
                    return database.query(FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES, projections, FavoriteMoviesContract.FavoriteMovieEntry._ID + "= ?", new String[]{String.valueOf(ContentUris.parseId(uri))}, null, null, null);
                }
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        switch(URI_MATCHER.match(uri)){
            case FAVORITE_MOVIES:
                final SQLiteDatabase database = favoriteMoviesDatabaseHelper.getWritableDatabase();
                long id = database.insert(FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES, null, contentValues);
                if(id > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    database.close();
                    return FavoriteMoviesContract.FavoriteMovieEntry.buildFavoriteMoviesWithMovieIdUri(id);
                }else{
                    throw new SQLException("Failed to insert row for " + uri);
                }
            case FAVORITE_MOVIES_WITH_ID:
                throw new UnsupportedOperationException("insert is not allowed for " + uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(selection != null || selectionArgs != null){
            throw new UnsupportedOperationException("selection arguments are not allowed for deletion on " + uri);
        }else{
            switch (URI_MATCHER.match(uri)){
                case FAVORITE_MOVIES:
                    throw new UnsupportedOperationException("deletion is not allowed for " + uri);
                case FAVORITE_MOVIES_WITH_ID:
                    final SQLiteDatabase database = favoriteMoviesDatabaseHelper.getWritableDatabase();
                    int answer = database.delete(FavoriteMoviesContract.FavoriteMovieEntry.TABLE_FAVORITE_MOVIES, FavoriteMoviesContract.FavoriteMovieEntry._ID + " = ?", new String[]{String.valueOf(ContentUris.parseId(uri))});
                    getContext().getContentResolver().notifyChange(uri, null);
                    database.close();
                    return answer;
            }
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("update is not allowed for " + uri);
    }
}
