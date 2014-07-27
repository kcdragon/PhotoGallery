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
    private TextView pageNumber;

    private ArrayList<GalleryItem> items;
    private int page;

    private int previousTotal;
    private boolean loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        page = 1;
        loading = true;
        previousTotal = 100;

        new FetchItemsTask().execute(page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        LinearLayout gridViewHeader = (LinearLayout) view.findViewById(R.id.gridViewHeader);
        pageNumber = (TextView) gridViewHeader.findViewById(R.id.pageNumber);
        pageNumber.setText("Page: " + page);

        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            public void onLoadMore(int page, int totalItemsCount) {
                new FetchItemsTask().execute(page);
                pageNumber.setText("Page: " + page);
            }
        });

        setupAdapter();

        return view;
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            if (PhotoGalleryFragment.this.items == null) {
                PhotoGalleryFragment.this.items = items;
            }
            else {
                PhotoGalleryFragment.this.items.addAll(items);
            }
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
