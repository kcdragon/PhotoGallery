package com.bignerdranch.android.photogallery;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import android.net.*;
import android.util.Log;

import org.xmlpull.v1.*;

import com.bignerdranch.android.photogallery.model.*;

public class FlickrFetchr {

    public static final String PREF_SEARCH_QUERY = "searchQuery";

    private static final String ENDPOINT          = "https://api.flickr.com/services/rest/";
    private static final String API_KEY           = "6766cedcc4914333cf29afc83bf7ea98";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH     = "flickr.photos.search";

    private static final String PARAM_EXTRAS      = "extras";
    private static final String PARAM_TEXT        = "text";
    private static final String EXTRA_SMALL_URL   = "url_s";

    private static final String XML_PHOTO = "photo";

    public GalleryItemCollection fetchItems(int page) {
        String url = Uri.parse(ENDPOINT).buildUpon().
            appendQueryParameter("method", METHOD_GET_RECENT).
            appendQueryParameter("api_key", API_KEY).
            appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL).
            appendQueryParameter("page", Integer.toString(page)).
            build().toString();
        return downloadGalleryItems(url);
    }

    public GalleryItemCollection search(String query, int page) {
        String url = Uri.parse(ENDPOINT).buildUpon().
            appendQueryParameter("method", METHOD_SEARCH).
            appendQueryParameter("api_key", API_KEY).
            appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL).
            appendQueryParameter(PARAM_TEXT, query).
            appendQueryParameter("page", Integer.toString(page)).
            build().toString();
        return downloadGalleryItems(url);
    }

    public GalleryItemCollection downloadGalleryItems(String url) {
        GalleryItemCollection collection = new GalleryItemCollection();

        try {
            String xmlString = getUrl(url);

            Log.i("FlickrFetchr", "Flickr XML: " + xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            collection = parseItems(parser);
        }
        catch (IOException e) {
            Log.e("FlickrFetchr", "Failed to fetch items" + e);
            collection.setItems(new ArrayList<GalleryItem>());
        }
        catch (XmlPullParserException e) {
            Log.e("FlickrFetchr", "Failed to parse items" + e);
            collection.setItems(new ArrayList<GalleryItem>());
        }

        return collection;
    }

    private GalleryItemCollection parseItems(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        int total = 0;

        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                items.add(item);
            }

            else if (eventType == XmlPullParser.START_TAG && parser.getName().equals("photos")) {
                total = Integer.parseInt(parser.getAttributeValue(null, "total"));
            }

            eventType = parser.next();
        }

        GalleryItemCollection collection = new GalleryItemCollection();
        collection.setItems(items);
        collection.setTotal(total);

        return collection;
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }
}
