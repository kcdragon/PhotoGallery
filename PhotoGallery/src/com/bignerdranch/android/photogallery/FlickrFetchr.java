package com.bignerdranch.android.photogallery;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import android.net.*;
import android.util.Log;

import org.xmlpull.v1.*;

import com.bignerdranch.android.photogallery.model.*;

public class FlickrFetchr {

    private static final String ENDPOINT          = "https://api.flickr.com/services/rest/";
    private static final String API_KEY           = "6766cedcc4914333cf29afc83bf7ea98";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS      = "extras";
    private static final String EXTRA_SMALL_URL   = "url_s";

    private static final String XML_PHOTO = "photo";

    public ArrayList<GalleryItem> fetchItems(int page) {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

        try {
            String url = Uri.parse(ENDPOINT).buildUpon().
                appendQueryParameter("method", METHOD_GET_RECENT).
                appendQueryParameter("api_key", API_KEY).
                appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL).
                appendQueryParameter("page", Integer.toString(page)).
                build().toString();
            String xmlString = getUrl(url);
            Log.i("FlickrFetchr", "Received xml: " + xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);
        }
        catch (IOException e) {
            Log.e("FlickrFetchr", "Failed to fetch items" + e);
        }
        catch (XmlPullParserException e) {
            Log.e("FlickrFetchr", "Failed to parse items" + e);
        }

        return items;
    }

    private void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {
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

            eventType = parser.next();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
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
