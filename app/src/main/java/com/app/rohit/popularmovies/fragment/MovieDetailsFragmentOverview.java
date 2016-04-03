package com.app.rohit.popularmovies.fragment;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.asynctask.DeleteImageAsyncTask;
import com.app.rohit.popularmovies.asynctask.InsertDeleteInDatabaseAsyncTask;
import com.app.rohit.popularmovies.asynctask.SaveImageAsyncTask;
import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.database.FavoriteMoviesContract;
import com.app.rohit.popularmovies.retrofit.ReviewData;
import com.app.rohit.popularmovies.retrofit.TrailerData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailsFragmentOverview extends Fragment implements
        InsertDeleteInDatabaseAsyncTask.InsertDeleteInDatabaseAsyncTaskReporter,
        SaveImageAsyncTask.SaveImageAsyncTaskReporter,
        DeleteImageAsyncTask.DeleteImageAsyncTaskReporter{
    private final String LOG_TAG = MovieDetailsFragmentOverview.class.getSimpleName();

    private MovieDetailsFragment parentFragment;

    @Bind(R.id.movie_details_title)
    protected TextView originalTitleTextView;
    @Bind(R.id.movie_details_release_date)
    protected TextView releaseDateTextView;
    @Bind(R.id.movie_details_vote_average)
    protected TextView voteAverageTextView;
    @Bind(R.id.movie_details_overview)
    protected TextView overviewTextView;
    @Bind(R.id.movie_details_overview_label)
    protected TextView overviewLabelTextView;
    @Bind(R.id.movie_details_poster)
    protected ImageView posterImageView;
    @Bind(R.id.movie_details_mark_favorite)
    protected Button markFavoriteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details_overview, container, false);
        ButterKnife.bind(this, view);

        try {
            parentFragment = (MovieDetailsFragment) getParentFragment();
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "parent fragment is not " + MovieDetailsFragment.class.getSimpleName());
        }
        if (parentFragment != null) {
            parentFragment.setChildFragmentHook(this);
        }

        updateYourUI();

        markFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (button.getText().toString().equals(getString(R.string.fragment_movie_details_fav_button_mark))) {
                    startSaving();
                } else {
                    startDeleting();
                }
            }
        });
        return view;
    }

    public void updateYourUI(){
        originalTitleTextView.setText(parentFragment.getMovieTitle());
        releaseDateTextView.setText(parentFragment.getReleaseDate());
        voteAverageTextView.setText(String.valueOf(parentFragment.getRating()));

        String overview = parentFragment.getOverview();
        if (overview == null || overview.trim().equals("")) {
            overviewLabelTextView.setText(getText(R.string.fragment_movie_details_no_overview));
        } else {
            overviewLabelTextView.setText(getText(R.string.fragment_movie_details_overview));
            overviewTextView.setText(overview);
        }

        if(parentFragment.isFavorite() && parentFragment.isDatabaseLoadingRequired()){
            posterImageView.setImageBitmap(parentFragment.getBitmap());
        }else{
            String path = parentFragment.getPosterPath();
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + path)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            posterImageView.setImageBitmap(bitmap);
                            parentFragment.updateBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }
        updateFavoriteButtonText(parentFragment.getFavoriteButtonStatus());

        markFavoriteButton.setEnabled(false);
        if(!parentFragment.isFavorite() || (parentFragment.isFavorite() && !parentFragment.isDatabaseLoadingRequired())){
            if (parentFragment.getMovieDetailsDownloadStatus() == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED) {
                markFavoriteButton.setEnabled(true);
            }
        }else if(parentFragment.isDatabaseLoadingDone()){
            markFavoriteButton.setEnabled(true);
        }
    }

    private void startSaving(){
        new SaveImageAsyncTask(MovieDetailsFragmentOverview.this, parentFragment.getMovieId(), parentFragment.getBitmap()).execute();
        markFavoriteButton.setEnabled(false);
    }

    private void startDeleting(){
        new DeleteImageAsyncTask(MovieDetailsFragmentOverview.this, parentFragment.getMovieId()).execute();
        markFavoriteButton.setEnabled(false);
    }

    private void deleteMovieFromDatabase(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry._ID, parentFragment.getMovieId());
        new InsertDeleteInDatabaseAsyncTask(this, Enums.DATABASE_OPERATION_TYPE.DELETE_FAVORITE_MOVIE, contentValues).execute();
    }

    private void saveMovieToDatabase(String posterDatabasePath){
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry._ID, parentFragment.getMovieId());
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TITLE, parentFragment.getMovieTitle());
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, parentFragment.getReleaseDate());
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RATING, parentFragment.getRating());
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_OVERVIEW, parentFragment.getOverview());
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, posterDatabasePath);
        Gson gson = new Gson();
        List<TrailerData.Trailer> trailersList = parentFragment.getTrailersList();
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TRAILERS_JSON, gson.toJson(trailersList, new TypeToken<List<TrailerData.Trailer>>() {}.getType()));
        List<ReviewData.Review> reviewsList = parentFragment.getReviewsList();
        contentValues.put(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_REVIEWS_JSON, gson.toJson(reviewsList, new TypeToken<List<ReviewData.Review>>() {
        }.getType()));
        new InsertDeleteInDatabaseAsyncTask(this, Enums.DATABASE_OPERATION_TYPE.INSERT_FAVORITE_MOVIE, contentValues).execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (parentFragment != null) {
            parentFragment.setChildFragmentHook(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (parentFragment != null) {
            parentFragment.destroyChildFragmentHook(this);
        }
    }




    /*
    to be called from parentFragment
     */

    public void enableFavoriteButton() {
        markFavoriteButton.setEnabled(true);
    }

    public void updateFavoriteButtonText(Enums.FAVORITE_BUTTON_STATUS status){
        switch(status){
            case MARK:
                markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_mark));
                break;
            case UNMARK:
                markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_unmark));
                break;
        }
    }






    /*
    callbacks for InsertDeleteInDatabaseAsyncTask.InsertDeleteInDatabaseAsyncTaskReporter
     */

    @Override
    public void updateInsertionResult(boolean status) {
        markFavoriteButton.setEnabled(true);
        if (status) {
            Toast.makeText(getActivity(), getString(R.string.toast_favorite_movie_save_successful), Toast.LENGTH_SHORT).show();
            markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_unmark));
            parentFragment.addedToFavorites();
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_favorite_movie_save_unsuccessful), Toast.LENGTH_SHORT).show();
            markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_mark));
        }
    }

    @Override
    public void updateDeletionResult(boolean status) {
        markFavoriteButton.setEnabled(true);
        if (status) {
            Toast.makeText(getActivity(), getString(R.string.toast_favorite_movie_delete_successful), Toast.LENGTH_SHORT).show();
            markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_mark));
            parentFragment.removedFromFavorites();
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_favorite_movie_delete_unsuccessful), Toast.LENGTH_SHORT).show();
            markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_unmark));
        }
    }






    /*
    callbacks for SaveImageAsyncTask.SaveImageAsyncTaskReporter
     */

    @Override
    public void updateDatabasePosterPath(String path) {
        saveMovieToDatabase(path);
    }






    /*
    callbacks for DeleteImageAsyncTask.DeleteImageAsyncTaskReporter
     */
    @Override
    public void updateImageDeletionStatus(Boolean status) {
        if(status){
            deleteMovieFromDatabase();
        }else{
            markFavoriteButton.setEnabled(true);
            markFavoriteButton.setText(getString(R.string.fragment_movie_details_fav_button_unmark));
        }
    }

}
