package com.example.kevin.popularmovies;

import android.provider.BaseColumns;

/**
 * Created by kchristiansen on 8/20/15.
 */
public class FavoritesContract {
    /**
     * The database entry.
     */
    public static final class FavoritesEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "movieId";
    }
}
