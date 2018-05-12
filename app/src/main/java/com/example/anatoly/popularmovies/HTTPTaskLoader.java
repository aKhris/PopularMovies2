package com.example.anatoly.popularmovies;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.example.anatoly.popularmovies.Utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

class HTTPTaskLoader extends AsyncTaskLoader<String> {
    private URL url;
    private String httpResponse;    // String for caching response


    HTTPTaskLoader(Context context, URL url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        if(url==null){return;}
        if(httpResponse!=null){
            deliverResult(httpResponse);
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        String gotString = "";
        try {
            gotString = NetworkUtils.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gotString;
    }

    @Override
    public void deliverResult(@Nullable String data) {
        httpResponse = data;
        super.deliverResult(httpResponse);
    }
}
