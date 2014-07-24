package com.bignerdranch.android.photogallery;

import java.io.IOException;
import java.util.ArrayList;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.text.*;
import android.text.format.*;
import android.view.*;
import android.util.Log;
import android.widget.*;

import com.bignerdranch.android.photogallery.model.*;

public class PhotoGalleryFragment extends Fragment {

    private GridView gridView;
    private ArrayList<GalleryItem> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);

        setupAdapter();

        return view;
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            PhotoGalleryFragment.this.items = items;
            setupAdapter();
        }
    }

    private void setupAdapter() {
        if (getActivity() == null || gridView == null) return;

        if (items != null) {
            gridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
                                                              android.R.layout.simple_gallery_item,
                                                              items));
        }
        else {
            gridView.setAdapter(null);
        }
    }
}
