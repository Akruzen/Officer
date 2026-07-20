package com.akruzen.officer.functions;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class SmsHelper {
    public static void prepareAndSendSms(Context context) throws SecurityException {
        TinyDB tinyDB = new TinyDB(context);
        boolean isAppendLocation = tinyDB.getBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED);
        if (isAppendLocation && PermissionHelper.isAlwaysFineLocationGranted(context)) {
            // Fetch the device's live location co-ordinates
            try {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    String locationUrl = "\nMaps Link: https://maps.google.com/?q=";
                    if (location != null) {
                        locationUrl += location.getLatitude() + "," + location.getLongitude();
                    } else {
                        locationUrl = "Could not fetch location.";
                    }
                    String message = tinyDB.getString(TinyDbKeys.SMS_TEXT_MESSAGE);
                    message += "\n" + locationUrl;
                    sendSms(context, tinyDB, message);
                }).addOnFailureListener(e -> {
                    Log.d("Sadashiv", "Error fetching location: " + e.getMessage());
                    String message = tinyDB.getString(TinyDbKeys.SMS_TEXT_MESSAGE);
                    message += "\nMaps Link: Could not fetch location. Try using Google's find my device";
                    sendSms(context, tinyDB, message);
                });
            } catch (Exception e) {
                Log.e("SmsHelper", "Error fetching location", e);
            }
        }
    }

    private static void sendSms(Context context, TinyDB tinyDB, String message) throws SecurityException {
        // Send SMS using the user's preferred SIM
        int simSlot = tinyDB.getInt(TinyDbKeys.SELECTED_SIM);
        String phoneNum = tinyDB.getString(TinyDbKeys.SMS_PHONE_NUMBER);
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        SubscriptionInfo simInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlot);
        if (simInfo != null) {
            int subId = simInfo.getSubscriptionId();
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class).createForSubscriptionId(subId);
            } else {
                smsManager = SmsManager.getSmsManagerForSubscriptionId(subId);
            }
            smsManager.sendTextMessage(phoneNum, null, message, null, null);
        } else {
            // Fallback to default SmsManager if no specific SIM info is found
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, message, null, null);
        }

        Log.d("Sadashiv", "SMS sent successfully: " + message);
    }
}
