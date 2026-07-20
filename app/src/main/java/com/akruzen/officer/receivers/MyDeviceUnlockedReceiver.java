package com.akruzen.officer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;

public class MyDeviceUnlockedReceiver extends BroadcastReceiver {

    TinyDB tinyDB;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            tinyDB = new TinyDB(context);
            tinyDB.putInt(TinyDbKeys.SMS_ALERT_IGNORE_CURR_COUNT, 0);
            tinyDB.putInt(TinyDbKeys.SMS_ALERT_COOLDOWN_CURR_COUNT, 0);
            Log.d("Sadashiv", "Screen unlocked! Ignore count and cooldown reset.");
        }
    }
}
