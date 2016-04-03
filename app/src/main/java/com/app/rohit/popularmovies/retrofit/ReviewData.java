package com.app.rohit.popularmovies.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ReviewData {
    List<Review> results;

    public ReviewData(List<Review> results) {
        this.results = results;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }

    public static class Review implements Parcelable{
        String author;
        String content;

        @Override
        public int describeContents() {
            return 0;
        }

        private Review(Parcel in){
            this.author = in.readString();
            this.content = in.readString();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.author);
            parcel.writeString(this.content);
        }

        public static final Parcelable.Creator<Review> CREATOR = new Creator<Review>() {
            @Override
            public Review createFromParcel(Parcel parcel) {
                return new Review(parcel);
            }

            @Override
            public Review[] newArray(int i) {
                return new Review[0];
            }
        };

        public Review(String author, String content) {
            this.author = author;
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
