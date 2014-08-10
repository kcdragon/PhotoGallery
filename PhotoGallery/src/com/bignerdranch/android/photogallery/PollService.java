package com.bignerdranch.android.photogallery;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bignerdranch.android.photogallery.model.*;

public class PollService extends IntentService {

    private static final int POLL_INTERVAL = 1000 * 60 * 5; // 5 minutes
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
        }
        else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context).
            edit().
            putBoolean(PREF_IS_ALARM_ON, isOn).
            commit();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    public PollService() {
        super("PollService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;

        if (!isNetworkAvailable) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        GalleryItemCollection itemCollection;

        if (query != null) {
            itemCollection = new FlickrFetchr().search(query, 1);
        }
        else {
            itemCollection = new FlickrFetchr().fetchItems(1);
        }

        if (itemCollection.getSize() == 0) {
            return;
        }

        String resultId = itemCollection.getItems().get(0).getId();

        if (!resultId.equals(lastResultId)) {
            Log.i("PollService", "Got a new result: " + resultId);

            Resources resources = getResources();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this).
                setTicker(resources.getString(R.string.new_pictures_title)).
                setSmallIcon(android.R.drawable.ic_menu_report_image).
                setContentTitle(resources.getString(R.string.new_pictures_title)).
                setContentText(resources.getString(R.string.new_pictures_text)).
                setContentIntent(pendingIntent).
                setAutoCancel(true).
                build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
        else {
            Log.i("PollService", "Got an old result: " + resultId);
        }

        prefs.edit().
            putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId).
            commit();

        Log.i("PollService", "Received an intent: " + intent);
    }
}
