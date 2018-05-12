package com.example.anatoly.popularmovies.db;

import com.example.anatoly.popularmovies.db.FavoriteContract.FavoriteEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteDbHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "favoritesDb.db";
    private static final int VERSION = 1;

    public FavoriteDbHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID                + " INTEGER PRIMARY KEY, " +
                FavoriteEntry.COLUMN_MOVIE_ID +     " INTEGER NOT NULL, " +
                FavoriteEntry.COLUMN_MOVIE_TITLE    + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_POSTER_PATH    + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_VOTE_AVR    + " REAL NOT NULL, " +
                FavoriteEntry.COLUMN_OVERVIEW    + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_RELEASE_DATE    + " TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE);
    }

    /*

     COLUMN_POSTER_PATH =
 COLUMN_VOTE_AVR = "v
 COLUMN_OVERVIEW = "o
 COLUMN_RELEASE_DATE

     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
