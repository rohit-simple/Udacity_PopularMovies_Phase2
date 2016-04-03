package com.app.rohit.popularmovies.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.adapter.ReviewAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailsFragmentReviews extends Fragment {
    private final String LOG_TAG = MovieDetailsFragmentReviews.class.getSimpleName();

    @Bind(R.id.movie_details_reviews_list_view) protected ListView reviewsListView;
    @Bind(R.id.movie_details_reviews_loading) protected ProgressBar loadingProgressBar;
    @Bind(R.id.movie_details_reviews_no_content) protected ImageView noContentImageView;

    private MovieDetailsFragment parentFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details_reviews, container, false);
        ButterKnife.bind(this, view);

        try{
            parentFragment = (MovieDetailsFragment) getParentFragment();
        }catch(ClassCastException e){
            Log.e(LOG_TAG, "parent fragment is not " + MovieDetailsFragment.class.getSimpleName());
        }
        if(parentFragment != null){
            parentFragment.setChildFragmentHook(this);
        }

        setLoadingProgressBar();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(parentFragment != null){
            parentFragment.conveyResultsToReviewsTabFragment();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(parentFragment != null){
            parentFragment.destroyChildFragmentHook(this);
        }
    }





    /*
    to be called from parentFragment
     */
    public void setLoadingProgressBar(){
        reviewsListView.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    public void setNoContent(){
        reviewsListView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.VISIBLE);
    }

    public void setReviews(){
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        reviewsListView.setVisibility(View.VISIBLE);

        ReviewAdapter reviewAdapter = new ReviewAdapter(getActivity(), parentFragment.getReviewsList());
        reviewsListView.setAdapter(reviewAdapter);
    }
}
