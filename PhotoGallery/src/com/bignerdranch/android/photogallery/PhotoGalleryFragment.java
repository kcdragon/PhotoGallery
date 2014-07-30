package com.bignerdranch.android.photogallery;

import java.io.IOException;
import java.util.ArrayList;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
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
    private TextView pageNumberView;

    private ArrayList<GalleryItem> items;
    private ThumbnailDownloader<ImageView> thumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

	new FetchItemsTask().execute(1);

	thumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
	thumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
	    public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
		if (isVisible()) {
		    imageView.setImageBitmap(thumbnail);
		}
	    }
	});
	thumbnailThread.start();
	thumbnailThread.getLooper();
	Log.i("PhotoGalleryFragment", "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        LinearLayout gridViewHeader = (LinearLayout) view.findViewById(R.id.gridViewHeader);
        pageNumberView = (TextView) gridViewHeader.findViewById(R.id.pageNumber);

	setPageNumber(1);

        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            public void onLoadMore(int page, int totalItemsCount) {
                new FetchItemsTask().execute(page);
		setPageNumber(page);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
	super.onDestroyView();
	thumbnailThread.clearQueue();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	thumbnailThread.quit();
	Log.i("PhotoGalleryFragment", "Background thread destroyed");
    }

    private void setPageNumber(int page) {
	pageNumberView.setText("Page: " + page);
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
		setupAdapter();
            }
            else {
                PhotoGalleryFragment.this.items.addAll(items);
		((ArrayAdapter) gridView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    private void setupAdapter() {
        if (getActivity() != null || gridView != null) {
	    gridView.setAdapter(new GalleryItemAdapter(items));
	}
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
	public GalleryItemAdapter(ArrayList<GalleryItem> items) {
	    super(getActivity(), 0, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    if (convertView == null) {
		convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
	    }

	    ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
	    imageView.setImageResource(R.drawable.brian_up_close);
	    GalleryItem item = getItem(position);
	    thumbnailThread.queueThumbnail(imageView, item.getUrl());

            preloadPreviousAndNextTenItems(position);

	    return convertView;
	}

        private void preloadPreviousAndNextTenItems(int position) {
            int start = position - 10;
            if (start < 0) {
                start = 0;
            }

            int end = position + 10;
            if (end >= getCount()) {
                end = getCount() - 1;
            }

            for (int i = start; i <= end; i++) {
                if (i != position) {
                    GalleryItem itemToCache = getItem(i);
                    thumbnailThread.queueThumbnailForPreload(itemToCache.getUrl());
                }
            }
        }
    }
}
