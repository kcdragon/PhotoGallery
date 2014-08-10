package com.bignerdranch.android.photogallery;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("StartupReceiver", "Received broadcast intent: " + intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = prefs.getBoolean(PollService.PREF_IS_ALARM_ON , false);
        PollService.setServiceAlarm(context, isOn);
    }
}
