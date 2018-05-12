package com.example.anatoly.popularmovies.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteContract {
    public static final String AUTHORITY = "com.example.anatoly.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class FavoriteEntry implements BaseColumns{
        public final static Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public final static String TABLE_NAME = "favorites";
        public final static String COLUMN_MOVIE_ID = "movieID";
        public final static String COLUMN_MOVIE_TITLE = "movieTitle";
        public final static String COLUMN_POSTER_PATH = "posterPath";
        public final static String COLUMN_VOTE_AVR = "voteAvr";
        public final static String COLUMN_OVERVIEW = "overview";
        public final static String COLUMN_RELEASE_DATE = "releaseDate";

    }
}
