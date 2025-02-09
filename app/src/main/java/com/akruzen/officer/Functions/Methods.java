package com.akruzen.officer.Functions;

import static com.akruzen.officer.Constants.TinyDbKeys.IS_ADMIN_ENABLED;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.akruzen.officer.DialogAccessibility;
import com.akruzen.officer.lib.TinyDB;

import java.util.List;

public class Methods {

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

    public static boolean isAllPermissionsGranted(Context context) {
        TinyDB tinyDB = new TinyDB(context);
        boolean isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(context, DialogAccessibility.class);
        boolean isAppAdmin = tinyDB.getBoolean(IS_ADMIN_ENABLED);
        return isAccessibilityServiceEnabled && isAppAdmin;
    }

}
