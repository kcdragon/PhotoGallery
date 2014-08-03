package com.bignerdranch.android.photogallery;

import android.app.SearchManager;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onNewIntent(Intent intent) {
        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i("PhotoGalleryActivity", "Received a new search query: " + query);

            PreferenceManager.getDefaultSharedPreferences(this).
                edit().
                putString(FlickrFetchr.PREF_SEARCH_QUERY, query).
                commit();
        }

        fragment.updateItems();
    }

    @Override
    public void startSearch(String initialQuery, boolean selectedInitialQuery, Bundle appSearchData, boolean globalSearch) {
        super.startSearch(initialQuery, true, appSearchData, globalSearch);
    }
}
