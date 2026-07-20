package com.akruzen.officer.functions;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityManager;

import androidx.core.content.ContextCompat;

import com.akruzen.officer.services.DialogAccessibilityService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionHelper {
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }
        return false;
    }

    public static boolean isAdminAccess(Context context) {
        AtomicBoolean isAdminAccessFlag = new AtomicBoolean(false);
        DevicePolicyManager policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (policyManager.getActiveAdmins() != null) {
            policyManager.getActiveAdmins().forEach(adminInfo -> {
                if (adminInfo.getPackageName().equals(context.getPackageName())) {
                    isAdminAccessFlag.set(true);
                }
            });
        }
        return isAdminAccessFlag.get();
    }

    public static boolean isSmsPermissionGranted(Context context) {
        // Check if app can send and read sms
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadPhoneStatePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAllMandatoryPermissionsGranted(Context context) {
        boolean isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(context, DialogAccessibilityService.class);
        boolean isAppAdmin = isAdminAccess(context);
        return isAccessibilityServiceEnabled && isAppAdmin;
    }

    public static boolean isAlwaysFineLocationGranted(Context context) {
        // Check if fine location access is allowed in background
        boolean fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return fineLocation && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return fineLocation;
    }
}
