package com.app.rohit.popularmovies.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.fragment.MainActivityFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviePosterDatabaseAdapter extends ArrayAdapter<MainActivityFragment.MovieIdAndPosterBitmapEncapsulated>{
    private final String LOG_TAG = MoviePosterDatabaseAdapter.class.getSimpleName();
    private final Integer MY_ROW_TYPE = 0;
    private MainActivityFragment mainActivityFragment;

    public MoviePosterDatabaseAdapter(Fragment fragment, List<MainActivityFragment.MovieIdAndPosterBitmapEncapsulated> list){
        super(fragment.getActivity(), 0, list);
        mainActivityFragment = (MainActivityFragment) fragment;
    }

    @Override
    public int getItemViewType(int position) {
        return MY_ROW_TYPE;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final MainActivityFragment.MovieIdAndPosterBitmapEncapsulated object = getItem(position);

        if(convertView == null || getItemViewType(position) != MY_ROW_TYPE){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_posters_cell_layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.setImageBitmap(object.getPosterBitmap());
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putLong(getContext().getString(R.string.extra_movie_id), object.getMovieId());
                bundle.putParcelable(getContext().getString(R.string.extra_poster_bitmap), object.getPosterBitmap());
                bundle.putBoolean(getContext().getString(R.string.extra_is_favorite_movie), true);
                mainActivityFragment.sendToDetailsScreenDatabaseCase(bundle);
            }
        });
        return convertView;
    }

    static class ViewHolder{
        @Bind(R.id.grid_movie_poster) ImageView imageView;

        ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }
}
