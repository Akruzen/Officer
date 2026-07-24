package com.akruzen.officer.services;

import static com.akruzen.officer.constants.TinyDbKeys.IS_MASTER_ENABLED;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.functions.SmsHelper;
import com.akruzen.officer.lib.TinyDB;
import com.akruzen.officer.receivers.MyDeviceUnlockedReceiver;

@SuppressLint("AccessibilityPolicy")
public class DialogAccessibilityService extends AccessibilityService {
    private MyDeviceUnlockedReceiver unlockedReceiver;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AccessibilityService", "Service is connected and running!");
        // Retrieve the existing configuration to preserve XML settings
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            info = new AccessibilityServiceInfo();
        }

        // Only listen for window state change events
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        // Set the type of feedback your service provides
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

        // Restrict to System UI package to eliminate system-wide overhead
        info.packageNames = new String[]{"com.android.systemui"};

        info.flags |= AccessibilityServiceInfo.DEFAULT;

        // Deliver events instantly (0ms delay)
        info.notificationTimeout = 0;

        this.setServiceInfo(info);

        unlockedReceiver = new MyDeviceUnlockedReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(unlockedReceiver, filter);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.d("Sadashiv", "onAccessibilityEvent: " + event.getEventType());
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                TinyDB tinyDB = new TinyDB(this);

                // Safe null check for package name
                CharSequence pkgNameChar = event.getPackageName();
                String packageName = pkgNameChar != null ? pkgNameChar.toString() : "";

                if (packageName.equals("com.android.systemui")) {
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                    boolean isScreenLocked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();

                    if (!isScreenLocked) {
                        Intent intent = new Intent("com.akruzen.officer.SYSTEM_UI_EVENT");
                        intent.putExtra("eventClassName", event.getClassName());
                        sendBroadcast(intent);
                    } else if (shouldScreenGoToSleep(event, tinyDB)) {

                        // Dismiss power menu immediately (Simulate Back Button)
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        Log.i("AccessibilityService", "Power menu dismissed via BACK action");

                        // Lock the device screen immediately
                        devicePolicyManager.lockNow();
                        Log.i("AccessibilityService", "Device locked");

                        // Perform other operations
                        handleBroadcastEvents(this, tinyDB);
                        handleStrictSecurity(tinyDB);
                        handleSms(this, tinyDB);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean shouldScreenGoToSleep(AccessibilityEvent event, TinyDB tinyDB) {
        String customTrigger = tinyDB.getString(TinyDbKeys.CUSTOM_TRIGGER);
        boolean isPowerMenuFlag1 = event.toString().contains("ClassName: com.android.systemui.globalactions.GlobalActionsDialogLite$3");
        boolean isPowerMenuFlag2 = event.toString().contains("FullScreen: true");
        boolean isPowerMenuCustomFlag = tinyDB.getBoolean(TinyDbKeys.IS_CUSTOM_TRIGGER_ENABLED) && customTrigger != null &&
                !customTrigger.isEmpty() && event.toString().contains("ClassName: com.android.systemui." + customTrigger);
        boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);
        return (isPowerMenuFlag1 || isPowerMenuCustomFlag) && isPowerMenuFlag2 && isMasterEnabled;
    }

    private static void handleBroadcastEvents(Context context, TinyDB tinyDB) {
        boolean isBroadcastEnabled = tinyDB.getBoolean(TinyDbKeys.IS_BROADCAST_EVENT_ENABLED);
        boolean isRepeatBroadcastsChecked = tinyDB.getBoolean(TinyDbKeys.IS_REPEAT_BROADCAST_EVENTS_CHECKED);
        boolean isForceLockedBefore = tinyDB.getBoolean(TinyDbKeys.IS_DEVICE_FORCED_LOCKED);
        if (isBroadcastEnabled && (isRepeatBroadcastsChecked || !isForceLockedBefore)) {
            // Send a broadcast that can be intercepted by automation apps like Tasker or MacroDroid
            Intent intent = new Intent("com.akruzen.officer.ACTION_LOCK_TRIGGERED");
            intent.putExtra("timestamp", System.currentTimeMillis());
            context.sendBroadcast(intent);
            Log.d("AccessibilityService", "Sent trigger broadcast: com.akruzen.officer.ACTION_LOCK_TRIGGERED");
        }
    }

    private static void handleStrictSecurity(TinyDB tinyDB) {
        // Store the current system time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        tinyDB.putLong(TinyDbKeys.FORCED_SCREEN_LOCKED_TIME_IN_MILLIS, currentTimeMillis);
        // Set the forced lock flag to true. This is reset when user manually unlocks the screen
        tinyDB.putBoolean(TinyDbKeys.IS_DEVICE_FORCED_LOCKED, true);
    }

    private static void handleSms(Context context, TinyDB tinyDB) {
        if (tinyDB.getBoolean(TinyDbKeys.IS_SMS_ALERT_ENABLED)) {
            int currIgnoreCount = tinyDB.getInt(TinyDbKeys.SMS_ALERT_IGNORE_CURR_COUNT);
            int maxIgnoreCount = tinyDB.getInt(TinyDbKeys.SMS_ALERT_IGNORE_COUNT);
            int cooldownCurrCount = tinyDB.getInt(TinyDbKeys.SMS_ALERT_COOLDOWN_CURR_COUNT);
            int cooldownCount = tinyDB.getInt(TinyDbKeys.SMS_ALERT_COOLDOWN_COUNT);
            cooldownCount = cooldownCount == 0 ? -1 : cooldownCount;
            if (currIgnoreCount == maxIgnoreCount) {
                try {
                    if (cooldownCurrCount == cooldownCount + 1 /* Cooldown ends after +1 time */) {
                        cooldownCurrCount = 0;
                    }
                    if (cooldownCurrCount == 0) {
                        SmsHelper.prepareAndSendSms(context);
                    }
                    if (cooldownCount == -1) {
                        // Increase currIgnoreCount so that the 'if' outside 'try' becomes always false unless unlocked.
                        // This way, the SMS will be sent only once
                        tinyDB.putInt(TinyDbKeys.SMS_ALERT_IGNORE_CURR_COUNT, currIgnoreCount + 1);
                    } else {
                        tinyDB.putInt(TinyDbKeys.SMS_ALERT_COOLDOWN_CURR_COUNT, cooldownCurrCount + 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Increment the value of curr by 1
                tinyDB.putInt(TinyDbKeys.SMS_ALERT_IGNORE_CURR_COUNT, currIgnoreCount + 1);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("AccessibilityService", "Service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unlockedReceiver != null) {
            unregisterReceiver(unlockedReceiver);
            unlockedReceiver = null;
        }
    }
}
