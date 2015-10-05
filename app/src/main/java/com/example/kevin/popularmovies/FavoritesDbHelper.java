package com.example.kevin.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kchristiansen on 8/20/15.
 */
public class FavoritesDbHelper extends SQLiteOpenHelper {
    /** the database version. */
    private static final int DATABASE_VERSION = 1;
    /** the database name. */
    private static final String DATABASE_NAME = "favorites.db";

    /**
     * Constructor.
     * @param context --
     */
    public FavoritesDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE "
                + FavoritesContract.FavoritesEntry.TABLE_NAME + "("
                + FavoritesContract.FavoritesEntry._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FavoritesContract.FavoritesEntry.COLUMN_ID
                + " TEXT NOT NULL)";
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db,
                          final int oldVersion,
                          final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "
                + FavoritesContract.FavoritesEntry.TABLE_NAME);
        onCreate(db);
    }
}
