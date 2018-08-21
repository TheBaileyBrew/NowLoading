package com.thebaileybrew.nowloading.queryelements.gamesquery;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

class gameLoader extends AsyncTaskLoader<List<game>> {
    private static final String TAG = gameLoader.class.getSimpleName();

    private final String gameUrl;

    private gameLoader(Context context, String url) {
        super(context);
        gameUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.v("Starting Loader", "Yes");
        forceLoad();
    }

    @Override
    public List<game> loadInBackground() {
        if (gameUrl == null) {
            return null;
        }
        Log.v("Loading in BG", "Yes");
        return super.onLoadInBackground();
    }
}
