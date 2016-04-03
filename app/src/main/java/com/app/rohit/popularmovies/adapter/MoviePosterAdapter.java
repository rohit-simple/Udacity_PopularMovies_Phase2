package com.app.rohit.popularmovies.adapter;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.fragment.MainActivityFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviePosterAdapter extends ArrayAdapter<String> {
    private final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    private final Integer MY_ROW_TYPE = 0;
    private MainActivityFragment mainActivityFragment;

    public MoviePosterAdapter(Fragment fragment, List<String> list){
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
        String path = getItem(position);

        if(convertView == null || getItemViewType(position) != MY_ROW_TYPE){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_posters_cell_layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String httpPath = "http://image.tmdb.org/t/p/w185" + path;

        Picasso picasso = Picasso.with(getContext());
        picasso.setIndicatorsEnabled(true); //added indicators to actually see power of picasso that it actually caches same images :D

        final ImageView finalPicImageView = viewHolder.imageView;   //viewHolder imageView's final copy made so as to access it in inner class
                                                                    //cant make it global as will be different for every position
        picasso.load(httpPath)
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_image)
                .into(viewHolder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        finalPicImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mainActivityFragment.sendToDetailsScreenDownloadCase(position);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        /*
                        we reuse the cell
                        so it may be the case that the imageview in reused cell had onclicklistener attached to it to go to details screen
                        because for that case, image was successfully downloaded
                        so to remove that onclicklistener
                         */
                        finalPicImageView.setOnClickListener(null);
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
