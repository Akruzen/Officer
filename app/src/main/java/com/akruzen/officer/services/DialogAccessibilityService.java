package com.akruzen.officer.services;

import static com.akruzen.officer.constants.TinyDbKeys.IS_MASTER_ENABLED;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;

@SuppressLint("AccessibilityPolicy")
public class DialogAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AccessibilityService", "Service is connected and running!");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Set the type of events that this service wants to listen to. Others
        // aren't passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

        // Default services are invoked only if no package-specific services are
        // present for the type of AccessibilityEvent generated. This service is
        // app-specific, so the flag isn't necessary. For a general-purpose service,
        // consider setting the DEFAULT flag.

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("Sadashiv", "onAccessibilityEvent: " + event.getEventType());
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                TinyDB tinyDB = new TinyDB(this);
                String packageName = event.getPackageName().toString();
                Log.d("Sadashiv", "Window state change detected with event: " + event);

                if (packageName.equals("com.android.systemui")) {
                    Log.d("Sadashiv", "System UI detected with event: " + event);
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                    boolean isPowerMenuFlag1 = event.toString().contains("ClassName: com.android.systemui.globalactions.GlobalActionsDialogLite$3");
                    boolean isPowerMenuFlag2 = event.toString().contains("FullScreen: true");
                    boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);
                    boolean isScreenLocked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
                    if (isPowerMenuFlag1 && isPowerMenuFlag2 && isScreenLocked && isMasterEnabled) {
                        // Lock the device screen
                        devicePolicyManager.lockNow();
                        Log.i("AccessibilityService", "Device locked");
                        // Store the current system time in milliseconds
                        long currentTimeMillis = System.currentTimeMillis();
                        tinyDB.putLong(TinyDbKeys.FORCED_SCREEN_LOCKED_TIME_IN_MILLIS, currentTimeMillis);
                        // Set the forced lock flag to true
                        tinyDB.putBoolean(TinyDbKeys.IS_DEVICE_FORCED_LOCKED, true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("AccessibilityService", "Service interrupted");
    }
}
