package com.example.anatoly.popularmovies.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FavoriteContentProvider extends ContentProvider {

    //match for the whole list of favorites:
    public static final int MATCH_MOVIES = 100;
    //match for the single movie
    public static final int MATCH_MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteDbHelper mFavoriteDbHelper;


    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoriteContract.AUTHORITY,FavoriteContract.PATH_MOVIES, MATCH_MOVIES);
        uriMatcher.addURI(FavoriteContract.AUTHORITY,FavoriteContract.PATH_MOVIES+"/#", MATCH_MOVIE_WITH_ID);
        return uriMatcher;
    }




    @Override
    public boolean onCreate() {
        mFavoriteDbHelper = new FavoriteDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match){
            case MATCH_MOVIES:
                cursor = db.query(
                        FavoriteContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MATCH_MOVIE_WITH_ID:
                String movieID = uri.getPathSegments().get(1);
                cursor = db.query(
                        FavoriteContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID+"=?",
                        new String[]{movieID},
                        null,
                        null,
                        sortOrder
                );
                break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Set a notification URI on the Cursor and return that Cursor
        if(getContext()!=null && getContext().getContentResolver()!=null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match){
            case MATCH_MOVIE_WITH_ID:
                long id = db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, values);

                if(id>0){
                    returnUri = ContentUris.withAppendedId(FavoriteContract.FavoriteEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert movie: " + uri);
                }
                break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(getContext()!=null && getContext().getContentResolver()!=null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deletedCount;
        switch (match){
            case MATCH_MOVIE_WITH_ID:
                String movieID = uri.getPathSegments().get(1);
                deletedCount = db.delete(FavoriteContract.FavoriteEntry.TABLE_NAME,
                        FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID+"=?",
                        new String[]{movieID});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(deletedCount!=0 && getContext()!=null && getContext().getContentResolver()!=null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedCount;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
