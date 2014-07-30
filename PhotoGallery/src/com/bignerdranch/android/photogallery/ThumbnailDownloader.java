package com.bignerdranch.android.photogallery;

import java.io.*;
import java.util.*;

import android.graphics.*;
import android.os.*;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    private Handler handler;
    private Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler responseHandler;
    private Listener<Token> listener;
    private LruCache<String, Bitmap> thumbnailCache;

    public interface Listener<Token> {
	void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
	this.listener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
	super("ThumbnailDownloader");
	this.responseHandler = responseHandler;

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        thumbnailCache = new LruCache<String, Bitmap>(cacheSize);
    }

    public void queueThumbnail(Token token, String url) {
	Log.i("ThumbnailDownloader", "Got a URL: " + url);
	requestMap.put(token, url);

	handler.
	    obtainMessage(MESSAGE_DOWNLOAD, token).
	    sendToTarget();
    }

    public void queueThumbnailForPreload(String url) {
        Log.i("ThumbnailDownloader", "Got a URL for Preload: " + url);

        handler.
	    obtainMessage(MESSAGE_PRELOAD, url).
	    sendToTarget();
    }

    public void clearQueue() {
	handler.removeMessages(MESSAGE_DOWNLOAD);
        handler.removeMessages(MESSAGE_PRELOAD);
	requestMap.clear();
    }

    @Override
    protected void onLooperPrepared() {
	handler = new Handler() {
	    @Override
	    public void handleMessage(Message message) {
                switch(message.what) {
                case MESSAGE_DOWNLOAD:
		    Token token = (Token) message.obj;
		    Log.i("ThumbnailDownloader", "Got a request for url: " + requestMap.get(token));
		    handleRequest(token);
                    break;

                case MESSAGE_PRELOAD:
                    String url = (String) message.obj;
                    Log.i("ThumbnailDownloader", "Got a request for preloading url: " + url);
                    thumbnailForUrl(url);
                    break;
                }
	    }
	};
    }

    private void handleRequest(final Token token) {
        final String url = requestMap.get(token);

        if (url == null) return;

        final Bitmap bitmap = thumbnailForUrl(url);

        if (bitmap == null) return;

        responseHandler.post(new Runnable() {
            public void run() {
                if (requestMap.get(token) == url) {
                    requestMap.remove(token);
                    listener.onThumbnailDownloaded(token, bitmap);
                }
            }
        });
    }

    public Bitmap thumbnailForUrl(String url) {
        Bitmap bitmap;
        try {
            if (hasThumbnailInCache(url)) {
                bitmap = thumbnailCache.get(url);
                Log.i("ThumbnailDownloader", "Cache hit");
            }
            else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                thumbnailCache.put(url, bitmap);
                Log.i("ThumbnailDownloader", "Cache miss");
            }
        }
        catch (IOException e) {
	    Log.e("ThumbnailDownloader", "Error downloading image", e);
            bitmap = null;
	}
        return bitmap;
    }

    private boolean hasThumbnailInCache(String url) {
        return thumbnailCache.get(url) != null;
    }
}
