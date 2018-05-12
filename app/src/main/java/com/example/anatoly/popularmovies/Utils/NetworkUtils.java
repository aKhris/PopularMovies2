package com.example.anatoly.popularmovies.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.example.anatoly.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    // TODO: 11.04.18 Place valid API key here: 
    private static final String THEMOVIEDB_API_KEY = BuildConfig.API_KEY;
    private static final String THEMOVIEDB_BASE_MOVIE_URL = "http://api.themoviedb.org/3/movie";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";

    public enum IMAGE_SIZE{w92, w154, w185, w342, w500, w780, original}
    public enum SORT_ORDER{popular, top_rated}
    public enum SUB_PATH{reviews, videos}

    private static final String API_KEY_PARAM = "api_key";
    private static final String YOUTUBE_V_PARAM = "v";



    public static URL buildMoviesUrl(SORT_ORDER order){
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(order.toString())
                .appendQueryParameter(API_KEY_PARAM, THEMOVIEDB_API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildSubFolderURL(int id, SUB_PATH subPath){
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_MOVIE_URL).buildUpon()
                .appendPath(String.valueOf(id))
                .appendEncodedPath(subPath.toString())
                .appendQueryParameter(API_KEY_PARAM, THEMOVIEDB_API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Uri buildImageUri(String path, String imageSize){
        return Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendEncodedPath(imageSize)
                .appendEncodedPath(path)
                .build();
    }

    public static Uri buildYoutubeUri(String videoKey){
        return Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_V_PARAM, videoKey)
                .build();
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }


    /**
     * Solution got here:
     * https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm==null){return false;}
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
