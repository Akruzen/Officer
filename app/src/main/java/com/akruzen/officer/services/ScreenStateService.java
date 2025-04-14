package com.akruzen.officer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.akruzen.officer.R;
import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;

public class ScreenStateService extends Service {
    private BroadcastReceiver screenStateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenStateReceiver();
        startForeground(1, getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerScreenStateReceiver() {
        screenStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    TinyDB tinyDB = new TinyDB(context);
                    Log.d("ScreenStateService", "Screen ON detected at " + System.currentTimeMillis()/1000);
                    // Check if the device was forced locked
                    if (tinyDB.getBoolean(TinyDbKeys.IS_DEVICE_FORCED_LOCKED)) {
                        int cooldownTimeInSeconds = tinyDB.getInt(TinyDbKeys.COOLDOWN_TIMER_IN_MILLIS) / 1000;
                        if (cooldownTimeInSeconds == 0) cooldownTimeInSeconds = 5; // Default value
                        long timeLeftInMillis = System.currentTimeMillis() - tinyDB.getLong(TinyDbKeys.FORCED_SCREEN_LOCKED_TIME_IN_MILLIS);
                        if (timeLeftInMillis < cooldownTimeInSeconds * 1000) {
                            // Lock the device again
                            try {
                                System.out.println("Cooldown timer is still active.");
                                System.out.println("Wait for " + (cooldownTimeInSeconds - timeLeftInMillis/1000) + " seconds");
                                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                                devicePolicyManager.lockNow();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Couldn't lock the device", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Let the user unlock the device
                            tinyDB.putBoolean(TinyDbKeys.IS_DEVICE_FORCED_LOCKED, false);
                        }
                    } // else no theft is detected, continue as usual
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, filter);
    }

    private Notification getNotification() {
        String channelId = "screen_state_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Screen State Service", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Screen State Service")
                .setContentText("Monitoring screen ON/OFF events")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}
