package com.app.rohit.popularmovies.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.activity.SettingsActivity;
import com.app.rohit.popularmovies.adapter.MoviePosterAdapter;
import com.app.rohit.popularmovies.adapter.MoviePosterDatabaseAdapter;
import com.app.rohit.popularmovies.asynctask.LoadImageAsyncTask;
import com.app.rohit.popularmovies.constant.Enums;
import com.app.rohit.popularmovies.database.FavoriteMoviesContract;
import com.app.rohit.popularmovies.retrofit.DiscoverMovieData;
import com.app.rohit.popularmovies.service.MoviesDownloadService;
import com.app.rohit.popularmovies.service.ReviewsDownloadService;
import com.app.rohit.popularmovies.service.TrailersDownloadService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LoadImageAsyncTask.LoadImageAsyncTaskReporter {

    /*
    this class will be used as structure for MoviePosterDatabaseAdapter
     */
    public static class MovieIdAndPosterBitmapEncapsulated {
        long movieId;
        Bitmap posterBitmap;

        public long getMovieId() {
            return movieId;
        }

        public Bitmap getPosterBitmap(){
            return posterBitmap;
        }
    }

    private List<DiscoverMovieData.MovieData> movieDetailsList;
    private List<LoadImageAsyncTask> loadImageAsyncTaskList;
    private MoviePosterAdapter moviePosterAdapter;
    private MoviePosterDatabaseAdapter moviePosterDatabaseAdapter;
    private MoviesDownloadServiceResponseListener moviesDownloadServiceResponseListener;
    private MainActivityFragmentToMainActivityCommunicator mainActivityFragmentToMainActivityCommunicator;
    private String sortByPreferenceValue;
    private boolean isDownloadingMoviesDone;
    private boolean hasUserPropagatedToSettingsScreen;

    private static final int FAVORITE_MOVIES_LIST_LOADER = 0;

    private final Integer FROM_MAINACTIVITY_TO_SETTINGS = 111;
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private final String INSTANCE_STATE_KEY_DOWNLOADED_MOVIE_LIST = "instance_state_key_downloaded_movie_list";

    @Bind(R.id.grid_movies)
    protected GridView gridView;
    @Bind(R.id.movie_posters_loading)
    protected ProgressBar loadingProgressBar;
    @Bind(R.id.movie_posters_no_content)
    protected ImageView noContentImageView;

    public interface MainActivityFragmentToMainActivityCommunicator {
        void showDetails(Bundle bundle);

        void clearDetails();
    }

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        hasUserPropagatedToSettingsScreen = false;

        if (getString(R.string.action_sort_by_value_most_popular).equals(sortByPreferenceValue) || getString(R.string.action_sort_by_value_highest_rated).equals(sortByPreferenceValue) || !getString(R.string.action_sort_by_value_favorites).equals(sortByPreferenceValue)) {
            if(savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_STATE_KEY_DOWNLOADED_MOVIE_LIST)){
                isDownloadingMoviesDone = true; //this is hack to tell all other methods that no downloading is required i.e. this is favorites list
                movieDetailsList = savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY_DOWNLOADED_MOVIE_LIST);
            }else{
                isDownloadingMoviesDone = false;
            }
        }  else {
            Log.e(LOG_TAG, "sort by preference has some disgusting value!");
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!hasUserPropagatedToSettingsScreen){ //this check is made so that we don't end up re-downloading in case we have returned from settings screen
            mainActivityFragmentToMainActivityCommunicator.clearDetails();
            setLoadingProgressBar();
            readSortByPreference();
            decideTitle();
            if (getString(R.string.action_sort_by_value_most_popular).equals(sortByPreferenceValue) || getString(R.string.action_sort_by_value_highest_rated).equals(sortByPreferenceValue) || !getString(R.string.action_sort_by_value_favorites).equals(sortByPreferenceValue)) {
                if(isDownloadingMoviesDone){
                    if(movieDetailsList == null || movieDetailsList.isEmpty()){
                        setNoContent();
                    }else{
                        setListViewForDownload();
                    }
                }else{
                    Enums.SERVICE_STATUS moviesDownloadServiceStatus = MoviesDownloadService.getServiceStatus();
                    if (moviesDownloadServiceStatus == Enums.SERVICE_STATUS.NONE || moviesDownloadServiceStatus == Enums.SERVICE_STATUS.FINISHED_WITH_PROBLEMS) {
                        startMoviesDownloadService();
                    } else if (moviesDownloadServiceStatus == Enums.SERVICE_STATUS.FINISHED) {
                        Enums.SERVICE_DATA_STATUS moviesDownloadServiceDataStatus = MoviesDownloadService.getServiceDataStatus();
                        switch (moviesDownloadServiceDataStatus) {
                            case NONE:
                                Log.e(LOG_TAG, "onCreateView() -> moviesDownloadServiceStatus is FINISHED but still moviesDownloadServiceDataStatus is NONE");
                                break;
                            case ERROR:
                            case DATA_NULL_OR_EMPTY:
                                Toast.makeText(getActivity(), getString(R.string.main_activity_fragment_toast_internet_connectivity_prob), Toast.LENGTH_LONG).show();
                                setNoContent();
                                break;
                            case DATA_FULL:
                                movieDetailsList = MoviesDownloadService.getMovieDetailsList();
                                setListViewForDownload();
                        }
                    } else {
                        IntentFilter intentFilter = new IntentFilter(getString(R.string.movies_service_broadcast_action));
                        moviesDownloadServiceResponseListener = new MoviesDownloadServiceResponseListener();
                        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(moviesDownloadServiceResponseListener, intentFilter);
                    }
                }
            }else if (getString(R.string.action_sort_by_value_favorites).equals(sortByPreferenceValue)) {
                setListViewForDatabase();
                getLoaderManager().restartLoader(FAVORITE_MOVIES_LIST_LOADER, null, this);
            }else {
                Log.e(LOG_TAG, "sort by preference has some disgusting value!");
            }
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mainActivityFragmentToMainActivityCommunicator = (MainActivityFragmentToMainActivityCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("activity must implement " + MainActivityFragmentToMainActivityCommunicator.class.getSimpleName());
        }
    }

    private void startMoviesDownloadService() {
        String sortMethod = null;
        if(getString(R.string.action_sort_by_value_most_popular).equals(sortByPreferenceValue)){
            sortMethod = "popularity.desc";
        }else if(getString(R.string.action_sort_by_value_highest_rated).equals(sortByPreferenceValue)){
            sortMethod = "vote_average.desc";
        }

        IntentFilter intentFilter = new IntentFilter(getString(R.string.movies_service_broadcast_action));
        moviesDownloadServiceResponseListener = new MoviesDownloadServiceResponseListener();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(moviesDownloadServiceResponseListener, intentFilter);

        Intent intent = new Intent(getActivity(), MoviesDownloadService.class);
        intent.putExtra(getString(R.string.extra_settings_sort_by), sortMethod);
        getActivity().startService(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(getActivity(), SettingsActivity.class), FROM_MAINACTIVITY_TO_SETTINGS);
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    public void setLoadingProgressBar() {
        gridView.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    public void setNoContent() {
        gridView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.VISIBLE);
    }

    private void decideTitle() {
        String title = getString(R.string.title_activity_main);
        if (getString(R.string.action_sort_by_value_most_popular).equals(sortByPreferenceValue)) {
            title = title + "-" + getString(R.string.action_sort_by_entry_most_popular);
        } else if (getString(R.string.action_sort_by_value_highest_rated).equals(sortByPreferenceValue)) {
            title = title + "-" + getString(R.string.action_sort_by_entry_highest_rated);
        } else if (getString(R.string.action_sort_by_value_favorites).equals(sortByPreferenceValue)) {
            title = title + "-" + getString(R.string.action_sort_by_entry_favorites);
        }
        getActivity().setTitle(title);
    }

    private void readSortByPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPreferences.contains(getString(R.string.action_sort_by_key))) {
            sortByPreferenceValue = sharedPreferences.getString(getString(R.string.action_sort_by_key), getString(R.string.action_sort_by_default));
        } else {
            sortByPreferenceValue = null;
        }
    }

    private void setListViewForDownload() {
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);

        List<String> moviePathList = new ArrayList<>();
        for (DiscoverMovieData.MovieData movieDetails : movieDetailsList) {
            moviePathList.add(movieDetails.getPoster_path());
        }

        moviePosterAdapter = new MoviePosterAdapter(this, moviePathList);

        gridView.setAdapter(moviePosterAdapter);
        isDownloadingMoviesDone = true;
    }

    private void setListViewForDatabase(){
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);

        if(loadImageAsyncTaskList == null){
            loadImageAsyncTaskList = new ArrayList<>();
        }else{
            loadImageAsyncTaskList.clear();
        }

        moviePosterDatabaseAdapter = new MoviePosterDatabaseAdapter(this, new ArrayList<MovieIdAndPosterBitmapEncapsulated>());
        gridView.setAdapter(moviePosterDatabaseAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_MAINACTIVITY_TO_SETTINGS) {
            //doing initial setup
            hasUserPropagatedToSettingsScreen = true;
            MoviesDownloadService.clear();
            mainActivityFragmentToMainActivityCommunicator.clearDetails();
            setLoadingProgressBar();
            readSortByPreference();
            decideTitle();

            if (getString(R.string.action_sort_by_value_highest_rated).equals(sortByPreferenceValue) || getString(R.string.action_sort_by_value_most_popular).equals(sortByPreferenceValue)) {
                isDownloadingMoviesDone = false;
                startMoviesDownloadService();
            } else {
                isDownloadingMoviesDone = true;
                setListViewForDatabase();
                getLoaderManager().restartLoader(FAVORITE_MOVIES_LIST_LOADER, null, this);
            }
        }
    }

    public void sendToDetailsScreenDownloadCase(Integer position) {
        ReviewsDownloadService.clear();     //needed so that ReviewsDownloadService can be restarted
        TrailersDownloadService.clear();    //needed so that TrailersDownloadService can be restarted

        DiscoverMovieData.MovieData movieDetails = movieDetailsList.get(position);
        if (movieDetails != null) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.extra_original_title), movieDetails.getOriginal_title());
            bundle.putString(getString(R.string.extra_overview), movieDetails.getOverview());
            bundle.putString(getString(R.string.extra_poster_path), movieDetails.getPoster_path());
            bundle.putString(getString(R.string.extra_release_date), movieDetails.getRelease_date());
            bundle.putDouble(getString(R.string.extra_vote_average), movieDetails.getVote_average());
            bundle.putLong(getString(R.string.extra_movie_id), movieDetails.getId());
            mainActivityFragmentToMainActivityCommunicator.showDetails(bundle);
        } else {
            Log.d(LOG_TAG, "sendToDetailsScreenDownloadCase() -> movieDetails retrieved is null");
        }
    }

    public void sendToDetailsScreenDatabaseCase(Bundle bundle){
        mainActivityFragmentToMainActivityCommunicator.showDetails(bundle);
    }

    @Override
    public void onStop() {
        super.onStop();

        hasUserPropagatedToSettingsScreen = false;

        if (moviePosterAdapter != null) {
            moviePosterAdapter.clear();
        }
        if(moviePosterDatabaseAdapter != null){
            moviePosterDatabaseAdapter.clear();
        }

        if(loadImageAsyncTaskList != null){
            for(LoadImageAsyncTask loadImageAsyncTask: loadImageAsyncTaskList){
                if(loadImageAsyncTask.getStatus() == AsyncTask.Status.PENDING || loadImageAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                    loadImageAsyncTask.cancel(true);
                }
            }
            loadImageAsyncTaskList.clear();
        }

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(moviesDownloadServiceResponseListener);
        moviesDownloadServiceResponseListener = null;

        Intent intent = new Intent(getActivity(), MoviesDownloadService.class);
        getActivity().stopService(intent);
    }

    /*
    this class is used to listen to results of service running in background to download movie list
     */
    private final class MoviesDownloadServiceResponseListener extends BroadcastReceiver {
        private final String LOG_TAG = MoviesDownloadServiceResponseListener.class.getSimpleName();

        //to avoid instantiation
        private MoviesDownloadServiceResponseListener() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.movies_service_broadcast_action))) {
                if (intent.hasExtra(getString(R.string.extra_service_data_status))) {
                    Enums.SERVICE_DATA_STATUS status = (Enums.SERVICE_DATA_STATUS) intent.getSerializableExtra(getString(R.string.extra_service_data_status));
                    switch (status) {
                        case ERROR:
                        case DATA_NULL_OR_EMPTY:
                            Toast.makeText(getActivity(), getString(R.string.main_activity_fragment_toast_internet_connectivity_prob), Toast.LENGTH_LONG).show();
                            setNoContent();
                            break;
                        case DATA_FULL:
                            if (intent.hasExtra(getString(R.string.extra_service_data))) {
                                movieDetailsList = intent.getParcelableArrayListExtra(getString(R.string.extra_service_data));
                                setListViewForDownload();
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MoviesDownloadService.clear();
        ReviewsDownloadService.clear();
        TrailersDownloadService.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(!getString(R.string.action_sort_by_value_favorites).equals(sortByPreferenceValue)){
            if(isDownloadingMoviesDone){
                if(movieDetailsList == null || movieDetailsList.isEmpty()){
                    outState.putParcelableArrayList(INSTANCE_STATE_KEY_DOWNLOADED_MOVIE_LIST, null);
                }else{
                    outState.putParcelableArrayList(INSTANCE_STATE_KEY_DOWNLOADED_MOVIE_LIST, new ArrayList<Parcelable>(movieDetailsList));
                }
            }
        }
        super.onSaveInstanceState(outState);
    }




    /*
    callbacks for LoadImageAsyncTask.LoadImageAsyncTaskReporter
     */
    @Override
    public void sendingBitmap(Bitmap bitmap, Object object) {
        if(moviePosterDatabaseAdapter != null){
            MovieIdAndPosterBitmapEncapsulated data = new MovieIdAndPosterBitmapEncapsulated();
            data.movieId = (Long) object;
            data.posterBitmap = bitmap;
            moviePosterDatabaseAdapter.add(data);   //add to moviePosterDatabaseAdapter so that list is refreshed
        }
    }



    /*
    callbacks for LoaderManager.LoaderCallbacks<Cursor>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case FAVORITE_MOVIES_LIST_LOADER:
                return new CursorLoader(
                        getActivity(),
                        FavoriteMoviesContract.FavoriteMovieEntry.CONTENT_URI,
                        new String[]{
                                FavoriteMoviesContract.FavoriteMovieEntry._ID,
                                FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH
                        },
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null){
            if(data.getCount() > 0){
                int movieIdIndex = data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry._ID);
                int posterPathIndex = data.getColumnIndex(FavoriteMoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH);
                while(data.moveToNext()){
                    Long movieId = data.getLong(movieIdIndex);
                    String posterPath = data.getString(posterPathIndex);
                    LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(this, posterPath, movieId);
                    loadImageAsyncTask.execute();
                    loadImageAsyncTaskList.add(loadImageAsyncTask);
                }
            }else{
                setNoContent();
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
