package com.app.rohit.popularmovies.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.retrofit.ReviewData;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReviewAdapter extends ArrayAdapter<ReviewData.Review> {
    private final String LOG_TAG = ReviewAdapter.class.getSimpleName();
    private final Integer MY_ROW_TYPE = 0;

    public ReviewAdapter(Activity activity, List<ReviewData.Review> list){
        super(activity, 0, list);
    }

    @Override
    public int getItemViewType(int position) {
        return MY_ROW_TYPE;
    }

    static class ViewHolder{
        @Bind(R.id.review_cell_author) TextView author;
        @Bind(R.id.review_cell_content) TextView content;

        ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ReviewData.Review review = getItem(position);

        if(convertView == null || getItemViewType(position) != MY_ROW_TYPE){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_cell_layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.author.setText(getContext().getString(R.string.fragment_movie_details_author_details_by) + " " + review.getAuthor());
        viewHolder.content.setText(review.getContent());

        return convertView;
    }
}
