package com.app.rohit.popularmovies.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class DiscoverMovieData {
    List<MovieData> results;

    public DiscoverMovieData(List<MovieData> results) {
        this.results = results;
    }

    public List<MovieData> getResults() {
        return results;
    }

    public void setResults(List<MovieData> results) {
        this.results = results;
    }

    public static class MovieData implements Parcelable{
        String poster_path;
        String overview;
        String release_date;
        String original_title;
        Long id;
        Double vote_average;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(poster_path);
            parcel.writeString(overview);
            parcel.writeString(release_date);
            parcel.writeString(original_title);
            parcel.writeLong(id);
            parcel.writeDouble(vote_average);
        }

        private MovieData(Parcel in){
            this.poster_path = in.readString();
            this.overview = in.readString();
            this.release_date = in.readString();
            this.original_title = in.readString();
            this.id = in.readLong();
            this.vote_average = in.readDouble();
        }

        public static final Parcelable.Creator<MovieData> CREATOR = new Creator<MovieData>() {
            @Override
            public MovieData createFromParcel(Parcel parcel) {
                return new MovieData(parcel);
            }

            @Override
            public MovieData[] newArray(int i) {
                return new MovieData[0];
            }
        };

        public MovieData(String poster_path, String overview, String release_date, String original_title, Long id, Double vote_average) {
            this.poster_path = poster_path;
            this.overview = overview;
            this.release_date = release_date;
            this.original_title = original_title;
            this.id = id;
            this.vote_average = vote_average;
        }

        public String getPoster_path() {
            return poster_path;
        }

        public String getOverview() {
            return overview;
        }

        public String getRelease_date() {
            return release_date;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public Long getId() {
            return id;
        }

        public Double getVote_average() {
            return vote_average;
        }

        public void setPoster_path(String poster_path) {
            this.poster_path = poster_path;
        }

        public void setOverview(String overview) {
            this.overview = overview;
        }

        public void setRelease_date(String release_date) {
            this.release_date = release_date;
        }

        public void setOriginal_title(String original_title) {
            this.original_title = original_title;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setVote_average(Double vote_average) {
            this.vote_average = vote_average;
        }
    }
}
