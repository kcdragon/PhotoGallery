package com.bignerdranch.android.photogallery;

import java.io.*;
import java.util.*;

import android.graphics.*;
import android.os.*;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler handler;
    private Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler responseHandler;
    private Listener<Token> listener;

    public interface Listener<Token> {
	void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
	this.listener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
	super("ThumbnailDownloader");
	this.responseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
	handler = new Handler() {
	    @Override
	    public void handleMessage(Message message) {
		if (message.what == MESSAGE_DOWNLOAD) {
		    Token token = (Token) message.obj;
		    Log.i("ThumbnailDownloader", "Got a request for url: " + requestMap.get(token));
		    handleRequest(token);
		}
	    }
	};
    }

    public void queueThumbnail(Token token, String url) {
	Log.i("ThumbnailDownloader", "Got a URL: " + url);
	requestMap.put(token, url);

	handler.
	    obtainMessage(MESSAGE_DOWNLOAD, token).
	    sendToTarget();
    }

    public void clearQueue() {
	handler.removeMessages(MESSAGE_DOWNLOAD);
	requestMap.clear();
    }

    private void handleRequest(final Token token) {
	try {
	    final String url = requestMap.get(token);

	    if (url == null) return;

	    byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
	    final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	    Log.i("ThumbnailDownloader", "Bitmap created");

	    responseHandler.post(new Runnable() {
		public void run() {
		    if (requestMap.get(token) == url) {
			requestMap.remove(token);
			listener.onThumbnailDownloaded(token, bitmap);
		    }
		}
	    });
	}
	catch (IOException e) {
	    Log.e("ThumbnailDownloader", "Error downloading image", e);
	}
    }
}
