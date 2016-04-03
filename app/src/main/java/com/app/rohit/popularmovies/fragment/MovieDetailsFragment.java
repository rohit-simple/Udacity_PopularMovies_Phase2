package com.app.rohit.popularmovies.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.activity.MainActivity;
import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.database.FavoriteMoviesContract;
import com.app.rohit.popularmovies.retrofit.ReviewData;
import com.app.rohit.popularmovies.retrofit.TrailerData;
import com.app.rohit.popularmovies.service.ReviewsDownloadService;
import com.app.rohit.popularmovies.service.TrailersDownloadService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private static final int FAVORITE_MOVIE_DATA_LOAD = 0;

    private final String INSTANCE_STATE_KEY_TRAILERS_LIST = "instance_state_key_trailers_list";
    private final String INSTANCE_STATE_KEY_REVIEWS_LIST = "instance_state_key_reviews_list";

    private MovieDetailsFragmentOverview overviewTabFragment;
    private MovieDetailsFragmentTrailers trailersTabFragment;
    private MovieDetailsFragmentReviews reviewsTabFragment;
    private DownloadServiceResponseListener downloadServiceResponseListener;
    private Intent shareIntent;
    private Enums.MOVIE_DETAILS_DOWNLOAD_STATUS movieDetailsDownloadStatus;
    private MovieDetailsFragmentToMainActivityCommunicator movieDetailsFragmentToMainActivityCommunicator;

    private List<ReviewData.Review> reviewsList;
    private List<TrailerData.Trailer> trailersList;
    private Bitmap bitmap;
    private long movieId;
    private String movieTitle, releaseDate, overview, posterPath;
    private double rating;
    private boolean isFavorite, isDatabaseLoadingDone, isDatabaseLoadingRequired;
    private Enums.FAVORITE_BUTTON_STATUS favoriteButtonStatus;

    public interface MovieDetailsFragmentToMainActivityCommunicator {
        void movieRemovedFromFavorites();
    }

    public Enums.MOVIE_DETAILS_DOWNLOAD_STATUS getMovieDetailsDownloadStatus() {
        return movieDetailsDownloadStatus;
    }

    public List<ReviewData.Review> getReviewsList() {
        if (reviewsList != null) {
            return new ArrayList<>(reviewsList);
        }
        return null;
    }

    public List<TrailerData.Trailer> getTrailersList() {
        if (trailersList != null) {
            return new ArrayList<>(trailersList);
        }
        return null;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public long getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public double getRating() {
        return rating;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isDatabaseLoadingRequired() {
        return isDatabaseLoadingRequired;
    }

    public boolean isDatabaseLoadingDone() {
        return isDatabaseLoadingDone;
    }

    public Enums.FAVORITE_BUTTON_STATUS getFavoriteButtonStatus() {
        return favoriteButtonStatus;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey(getString(R.string.extra_movie_id)) || (bundle.getLong(getString(R.string.extra_movie_id)) < 0)) {
            return inflater.inflate(R.layout.fragment_movie_details_no_movieid, container, false);
        } else {
            FragmentTabHost tabHost = new FragmentTabHost(getActivity());
            if (tabHost != null) {
                final String OVERVIEW_TAB_TAG = "home";
                final String TRAILERS_TAB_TAG = "trailers";
                final String REVIEWS_TAB_TAG = "reviews";

                movieId = bundle.getLong(getString(R.string.extra_movie_id));
                getLoaderManager().initLoader(FAVORITE_MOVIE_DATA_LOAD, null, this);

                if (MainActivity.isPhone()) {
                    getActivity().setTitle(getString(R.string.title_activity_movie_details));
                }

                isDatabaseLoadingDone = false;
                isFavorite = false;
                favoriteButtonStatus = Enums.FAVORITE_BUTTON_STATUS.MARK;

                if (!bundle.containsKey(getString(R.string.extra_is_favorite_movie))) {
                    isDatabaseLoadingRequired = false;

                    movieTitle = bundle.getString(getString(R.string.extra_original_title));
                    releaseDate = bundle.getString(getString(R.string.extra_release_date));
                    rating = bundle.getDouble(getString(R.string.extra_vote_average));
                    overview = bundle.getString(getString(R.string.extra_overview));
                    posterPath = bundle.getString(getString(R.string.extra_poster_path));

                    movieDetailsDownloadStatus = Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.NOTHING_DOWNLOADED;

                    if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_STATE_KEY_REVIEWS_LIST)) {
                        reviewsList = savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY_REVIEWS_LIST);
                        updateStatusReviewsDownloaded();
                        conveyResultsToReviewsTabFragment();
                    } else {
                        Enums.SERVICE_STATUS reviewsDownloadServiceStatus = ReviewsDownloadService.getServiceStatus();
                        switch (reviewsDownloadServiceStatus) {
                            case FINISHED_WITH_PROBLEMS:
                                updateStatusReviewsDownloaded();
                            case NONE:
                                attachReviewsDownloadServiceListener();
                                startReviewsDownloadService();
                                if (reviewsTabFragment != null) {
                                    reviewsTabFragment.setLoadingProgressBar();
                                }
                                break;
                            case STARTED:
                                attachReviewsDownloadServiceListener();
                                if (reviewsTabFragment != null) {
                                    reviewsTabFragment.setLoadingProgressBar();
                                }
                                break;
                            case FINISHED:
                                updateStatusReviewsDownloaded();
                                reviewsList = ReviewsDownloadService.getReviewsList();
                                conveyResultsToReviewsTabFragment();
                        }
                    }

                    if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_STATE_KEY_TRAILERS_LIST)) {
                        trailersList = savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY_TRAILERS_LIST);
                        updateStatusTrailersDownloaded();
                        conveyResultsToTrailersTabFragment();
                    } else {
                        Enums.SERVICE_STATUS trailersDownloadServiceStatus = TrailersDownloadService.getServiceStatus();
                        switch (trailersDownloadServiceStatus) {
                            case FINISHED_WITH_PROBLEMS:
                                updateStatusTrailersDownloaded();
                            case NONE:
                                attachTrailersDownloadServiceListener();
                                startTrailersDownloadService();
                                if (trailersTabFragment != null) {
                                    trailersTabFragment.setLoadingProgressBar();
                                }
                                break;
                            case STARTED:
                                attachTrailersDownloadServiceListener();
                                if (trailersTabFragment != null) {
                                    trailersTabFragment.setLoadingProgressBar();
                                }
                                break;
                            case FINISHED:
                                updateStatusTrailersDownloaded();
                                trailersList = TrailersDownloadService.getTrailersList();
                                conveyResultsToTrailersTabFragment();
                        }
                    }
                } else {
                    isFavorite = true;
                    isDatabaseLoadingRequired = true;
                    bitmap = getArguments().getParcelable(getString(R.string.extra_poster_bitmap));
                }


                tabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_content);

                tabHost.addTab(tabHost.newTabSpec(OVERVIEW_TAB_TAG).setIndicator(getString(R.string.activity_movie_details_tab_home)), MovieDetailsFragmentOverview.class, bundle);
                tabHost.addTab(tabHost.newTabSpec(TRAILERS_TAB_TAG).setIndicator(getString(R.string.activity_movie_details_tab_trailers)), MovieDetailsFragmentTrailers.class, null);
                tabHost.addTab(tabHost.newTabSpec(REVIEWS_TAB_TAG).setIndicator(getString(R.string.activity_movie_details_tab_reviews)), MovieDetailsFragmentReviews.class, null);

                return tabHost;
            } else {
                Log.e(LOG_TAG, "tabHost is null");
            }
        }
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            movieDetailsFragmentToMainActivityCommunicator = (MovieDetailsFragmentToMainActivityCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("activity must implement " + MovieDetailsFragmentToMainActivityCommunicator.class.getSimpleName());
        }
    }


    public void setChildFragmentHook(Fragment fragment) {
        if (fragment instanceof MovieDetailsFragmentOverview) {
            overviewTabFragment = (MovieDetailsFragmentOverview) fragment;
            overviewTabFragment.updateYourUI();
        } else if (fragment instanceof MovieDetailsFragmentReviews) {
            reviewsTabFragment = (MovieDetailsFragmentReviews) fragment;
            conveyResultsToReviewsTabFragment();
        } else if (fragment instanceof MovieDetailsFragmentTrailers) {
            trailersTabFragment = (MovieDetailsFragmentTrailers) fragment;
            conveyResultsToTrailersTabFragment();
        }
    }

    public void destroyChildFragmentHook(Fragment fragment) {
        if (fragment instanceof MovieDetailsFragmentOverview) {
            overviewTabFragment = null;
        } else if (fragment instanceof MovieDetailsFragmentReviews) {
            reviewsTabFragment = null;
        } else if (fragment instanceof MovieDetailsFragmentTrailers) {
            trailersTabFragment = null;
        }
    }

    private void updateStatusReviewsDownloaded() {
        if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.NOTHING_DOWNLOADED) {
            movieDetailsDownloadStatus = Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.REVIEWS_DOWNLOADED;
        } else if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.TRAILERS_DOWNLOADED) {
            movieDetailsDownloadStatus = Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED;
            if (overviewTabFragment != null) {
                overviewTabFragment.enableFavoriteButton();
            }
        }
    }

    private void updateStatusTrailersDownloaded() {
        if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.NOTHING_DOWNLOADED) {
            movieDetailsDownloadStatus = Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.TRAILERS_DOWNLOADED;
        } else if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.REVIEWS_DOWNLOADED) {
            movieDetailsDownloadStatus = Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED;
            if (overviewTabFragment != null) {
                overviewTabFragment.enableFavoriteButton();
            }
        }
    }

    private void startReviewsDownloadService() {
        Intent startServiceIntent = new Intent(getActivity(), ReviewsDownloadService.class);
        startServiceIntent.putExtra(getString(R.string.extra_movie_id), movieId);
        getActivity().startService(startServiceIntent);
    }

    private void attachReviewsDownloadServiceListener() {
        IntentFilter intentFilter = new IntentFilter(getString(R.string.reviews_service_broadcast_action));
        if (downloadServiceResponseListener == null) {
            downloadServiceResponseListener = new DownloadServiceResponseListener();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(downloadServiceResponseListener, intentFilter);
    }

    private void startTrailersDownloadService() {
        Intent startServiceIntent = new Intent(getActivity(), TrailersDownloadService.class);
        startServiceIntent.putExtra(getString(R.string.extra_movie_id), movieId);
        getActivity().startService(startServiceIntent);
    }

    private void attachTrailersDownloadServiceListener() {
        IntentFilter intentFilter = new IntentFilter(getString(R.string.trailers_service_broadcast_action));
        if (downloadServiceResponseListener == null) {
            downloadServiceResponseListener = new DownloadServiceResponseListener();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(downloadServiceResponseListener, intentFilter);
    }

    public void conveyResultsToReviewsTabFragment() {
        if ((isDatabaseLoadingRequired && isDatabaseLoadingDone) ||
                (!isDatabaseLoadingRequired &&
                        (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.REVIEWS_DOWNLOADED ||
                            movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED))) {
            if (reviewsTabFragment != null) {
                if (reviewsList == null || reviewsList.isEmpty()) {
                    reviewsTabFragment.setNoContent();
                } else {
                    reviewsTabFragment.setReviews();
                }
            }
        }
    }

    public void conveyResultsToTrailersTabFragment() {
        if ((isDatabaseLoadingRequired && isDatabaseLoadingDone) ||
                (!isDatabaseLoadingRequired &&
                        (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.TRAILERS_DOWNLOADED ||
                                movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED))) {
            if (shareIntent == null && trailersList != null && !trailersList.isEmpty()) {
                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.putExtra(Intent.EXTRA_TEXT, Uri.parse("http://www.youtube.com/watch?v=" + trailersList.get(0).getKey()).toString());
                shareIntent.setType("text/plain");
                getActivity().invalidateOptionsMenu();
            }
            if (trailersTabFragment != null) {
                if (trailersList == null || trailersList.isEmpty()) {
                    trailersTabFragment.setNoContent();
                } else {
                    trailersTabFragment.setTrailers();
                }
            }
        }
    }

    private final class DownloadServiceResponseListener extends BroadcastReceiver {

        //to avoid instantiation
        private DownloadServiceResponseListener() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.reviews_service_broadcast_action))) {
                if (intent.hasExtra(getString(R.string.extra_service_data_status))) {
                    updateStatusReviewsDownloaded();
                    reviewsList = ReviewsDownloadService.getReviewsList();
                    conveyResultsToReviewsTabFragment();
                }
            } else if (intent.getAction().equals(getString(R.string.trailers_service_broadcast_action))) {
                if (intent.hasExtra(getString(R.string.extra_service_data_status))) {
                    updateStatusTrailersDownloaded();
                    trailersList = TrailersDownloadService.getTrailersList();
                    conveyResultsToTrailersTabFragment();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(downloadServiceResponseListener);

        Intent intent = new Intent(getActivity(), TrailersDownloadService.class);
        getActivity().stopService(intent);

        intent = new Intent(getActivity(), ReviewsDownloadService.class);
        getActivity().stopService(intent);

        downloadServiceResponseListener = null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_share);
        if (!menuItem.isVisible() && shareIntent != null) {
            menuItem.setVisible(true);
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            shareActionProvider.setShareIntent(shareIntent);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.TRAILERS_DOWNLOADED || movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED) {
            if (trailersList == null || trailersList.isEmpty()) {
                outState.putParcelableArrayList(INSTANCE_STATE_KEY_TRAILERS_LIST, null);
            } else {
                outState.putParcelableArrayList(INSTANCE_STATE_KEY_TRAILERS_LIST, new ArrayList<Parcelable>(trailersList));
            }
        }

        if (movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.REVIEWS_DOWNLOADED || movieDetailsDownloadStatus == Enums.MOVIE_DETAILS_DOWNLOAD_STATUS.EVERYTHING_DOWNLOADED) {
            if (reviewsList == null || reviewsList.isEmpty()) {
                outState.putParcelableArrayList(INSTANCE_STATE_KEY_REVIEWS_LIST, null);
            } else {
                outState.putParcelableArrayList(INSTANCE_STATE_KEY_REVIEWS_LIST, new ArrayList<Parcelable>(reviewsList));
            }
        }
        super.onSaveInstanceState(outState);
    }


    /*
    called from overviewTabFragment
     */
    public void updateBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void removedFromFavorites() {
        favoriteButtonStatus = Enums.FAVORITE_BUTTON_STATUS.MARK;
        if (isDatabaseLoadingRequired) {
            movieDetailsFragmentToMainActivityCommunicator.movieRemovedFromFavorites();
        }
    }

    public void addedToFavorites() {
        favoriteButtonStatus = Enums.FAVORITE_BUTTON_STATUS.UNMARK;
    }


    /*
    callbacks for LoaderManager.LoaderCallbacks<Cursor>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case FAVORITE_MOVIE_DATA_LOAD:
                return new CursorLoader(
                        getActivity(),
                        FavoriteMoviesContract.FavoriteMovieEntry.buildFavoriteMoviesWithMovieIdUri(movieId),
                        null,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1) {
            isFavorite = true;
            favoriteButtonStatus = Enums.FAVORITE_BUTTON_STATUS.UNMARK;
            if (isDatabaseLoadingRequired) {
                data.moveToNext();
                movieTitle = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TITLE));
                releaseDate = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE));
                rating = data.getLong(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_RATING));
                overview = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_OVERVIEW));
                String trailersJson = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_TRAILERS_JSON));
                trailersList = new Gson().fromJson(trailersJson, new TypeToken<List<TrailerData.Trailer>>() {
                }.getType());
                String reviewsJson = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_REVIEWS_JSON));
                reviewsList = new Gson().fromJson(reviewsJson, new TypeToken<List<ReviewData.Review>>() {
                }.getType());

                isDatabaseLoadingDone = true;
                if (overviewTabFragment != null) {
                    overviewTabFragment.enableFavoriteButton();
                    overviewTabFragment.updateYourUI();
                }
                conveyResultsToTrailersTabFragment();
                conveyResultsToReviewsTabFragment();
            } else {
                if (overviewTabFragment != null) {
                    overviewTabFragment.updateFavoriteButtonText(favoriteButtonStatus);
                }
            }
        } else {
            isFavorite = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
