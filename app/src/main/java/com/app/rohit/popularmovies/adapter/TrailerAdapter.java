package com.app.rohit.popularmovies.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.retrofit.TrailerData;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TrailerAdapter extends ArrayAdapter<TrailerData.Trailer>{
    private final String LOG_TAG = TrailerAdapter.class.getSimpleName();
    private final Integer MY_ROW_TYPE = 0;

    public TrailerAdapter(Activity activity, List<TrailerData.Trailer> list){
        super(activity, 0, list);
    }

    @Override
    public int getItemViewType(int position) {
        return MY_ROW_TYPE;
    }

    static class ViewHolder{
        @Bind(R.id.trailer_cell_name) TextView name;

        ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        TrailerData.Trailer trailer = getItem(position);

        if(convertView == null || getItemViewType(position) != MY_ROW_TYPE){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_cell_layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(trailer.getName());

        return convertView;
    }
}
