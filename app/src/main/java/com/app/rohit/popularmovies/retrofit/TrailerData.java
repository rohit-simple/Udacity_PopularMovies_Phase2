package com.app.rohit.popularmovies.retrofit;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class TrailerData {
    List<Trailer> results;

    public TrailerData(List<Trailer> results) {
        this.results = results;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }

    public static class Trailer implements Parcelable{
        String key;
        String name;
        String site;
        String type;

        public Trailer(String key, String name, String site, String type) {
            this.key = key;
            this.name = name;
            this.site = site;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.key);
            parcel.writeString(this.name);
            parcel.writeString(this.site);
            parcel.writeString(this.type);
        }

        private Trailer(Parcel in){
            this.key = in.readString();
            this.name = in.readString();
            this.site = in.readString();
            this.type = in.readString();
        }

        public static final Parcelable.Creator<Trailer> CREATOR = new Creator<Trailer>() {
            @Override
            public Trailer createFromParcel(Parcel parcel) {
                return new Trailer(parcel);
            }

            @Override
            public Trailer[] newArray(int i) {
                return new Trailer[0];
            }
        };
    }
}
