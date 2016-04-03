package com.app.rohit.popularmovies.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.adapter.TrailerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailsFragmentTrailers extends Fragment {
    private final String LOG_TAG = MovieDetailsFragmentTrailers.class.getSimpleName();

    private MovieDetailsFragment parentFragment;

    @Bind(R.id.movie_details_trailers_list_view) protected ListView trailersListView;
    @Bind(R.id.movie_details_trailers_loading) protected ProgressBar loadingProgressBar;
    @Bind(R.id.movie_details_trailers_no_content) protected ImageView noContentImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movie_details_trailers, container, false);
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
            parentFragment.conveyResultsToTrailersTabFragment();
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
        trailersListView.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    public void setNoContent(){
        trailersListView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.VISIBLE);
    }

    public void setTrailers(){
        loadingProgressBar.setVisibility(View.GONE);
        noContentImageView.setVisibility(View.GONE);
        trailersListView.setVisibility(View.VISIBLE);

        TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), parentFragment.getTrailersList());
        trailersListView.setAdapter(trailerAdapter);

        trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + parentFragment.getTrailersList().get(i).getKey())));
            }
        });
    }



}
