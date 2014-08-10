package com.bignerdranch.android.photogallery;

import android.view.Menu;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.preference.*;
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
        setHasOptionsMenu(true);

        updateItems();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Pull out the SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView) searchItem.getActionView();

            // Get the data from out searchable.xml as a SearchableInfo
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

            searchView.setSearchableInfo(searchInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_search:
            getActivity().onSearchRequested();
            return true;
        case R.id.menu_item_clear:
            PreferenceManager.getDefaultSharedPreferences(getActivity()).
                edit().
                putString(FlickrFetchr.PREF_SEARCH_QUERY, null).
                commit();
            updateItems();
            return true;
        case R.id.menu_item_toggle_polling:
            boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
            PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActivity().invalidateOptionsMenu();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        }
        else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    public void updateItems() {
        this.items = null;
        new FetchItemsTask().execute(1);
    }

    private void setPageNumber(int page) {
	pageNumberView.setText("Page: " + page);
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, GalleryItemCollection> {
        @Override
	protected GalleryItemCollection doInBackground(Integer... params) {
            Activity activity = getActivity();
            if (activity == null) {
                return new GalleryItemCollection();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(activity).
                getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
            Log.i("PhotoGalleryFragment", "FetchItemsTask received query: " + query);

            if (query != null) {
                return new FlickrFetchr().search(query, params[0]);
            }
            else {
                return new FlickrFetchr().fetchItems(params[0]);
            }
        }

        @Override
        protected void onPostExecute(GalleryItemCollection collection) {
            if (PhotoGalleryFragment.this.items == null) {
                PhotoGalleryFragment.this.items = collection.getItems();
                Toast.makeText(getActivity(), "" + collection.getTotal() + " results",  Toast.LENGTH_SHORT).show();
		setupAdapter();
            }
            else {
                PhotoGalleryFragment.this.items.addAll(collection.getItems());
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
