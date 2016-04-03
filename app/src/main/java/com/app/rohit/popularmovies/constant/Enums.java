package com.app.rohit.popularmovies.constant;

public class Enums {
    public enum SERVICE_STATUS {
        NONE,
        STARTED,
        FINISHED_WITH_PROBLEMS,
        FINISHED
    }

    public enum SERVICE_DATA_STATUS {
        NONE,
        ERROR,
        DATA_NULL_OR_EMPTY,
        DATA_FULL
    }

    public enum DATABASE_OPERATION_TYPE {
        INSERT_FAVORITE_MOVIE,
        DELETE_FAVORITE_MOVIE
    }

    public enum MOVIE_DETAILS_DOWNLOAD_STATUS{
        NOTHING_DOWNLOADED,
        TRAILERS_DOWNLOADED,
        REVIEWS_DOWNLOADED,
        EVERYTHING_DOWNLOADED
    }

    public enum FAVORITE_BUTTON_STATUS{
        UNMARK,
        MARK
    }
}
