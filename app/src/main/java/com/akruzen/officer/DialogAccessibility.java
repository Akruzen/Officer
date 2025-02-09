package com.akruzen.officer;

import static com.akruzen.officer.Constants.TinyDbKeys.IS_MASTER_ENABLED;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.akruzen.officer.lib.TinyDB;

public class DialogAccessibility extends AccessibilityService {

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
        // Log.d("AccessibilityService", "onAccessibilityEvent: " + event.getEventType());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            TinyDB tinyDB = new TinyDB(this);
            String packageName = event.getPackageName().toString();

            if (packageName.equals("com.android.systemui")) {
                Log.d("AccessibilityService", "System UI detected with event: " + event);
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                boolean isPowerMenuFlag1 = event.toString().contains("ClassName: com.android.systemui.globalactions.GlobalActionsDialogLite$ActionsDialogLite");
                boolean isPowerMenuFlag2 = event.toString().contains("FullScreen: true");
                boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);
                boolean isScreenLocked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
                if (isPowerMenuFlag1 && isPowerMenuFlag2 && isScreenLocked && isMasterEnabled) {
                    // Lock the device screen
                    try {
                        devicePolicyManager.lockNow();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i("AccessibilityService", "Device locked");
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("AccessibilityService", "Service interrupted");
    }
}
