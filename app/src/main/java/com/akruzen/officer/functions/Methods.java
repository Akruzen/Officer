package com.akruzen.officer.functions;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;

import com.akruzen.officer.services.DialogAccessibilityService;
import com.akruzen.officer.views.dialog.DialogLabels;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static boolean isAllPermissionsGranted(Context context) {
        boolean isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(context, DialogAccessibilityService.class);
        boolean isAppAdmin = isAdminAccess(context);
        return isAccessibilityServiceEnabled && isAppAdmin;
    }

    public static AlertDialog getAlertDialog(Context context, DialogLabels dialogLabels) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(dialogLabels.getTitle());
        builder.setMessage(dialogLabels.getMessage());
        if (dialogLabels.getPositiveText() != null) {
            builder.setPositiveButton(dialogLabels.getPositiveText(), (dialog, which) -> {
                if (dialogLabels.getCallback() != null) {
                    dialogLabels.getCallback().onPositiveClick(dialog);
                } else {
                    dialog.dismiss();
                }
            });
        }
        if (dialogLabels.getNegativeText() != null) {
            builder.setNegativeButton(dialogLabels.getNegativeText(), (dialog, which) -> {
                if (dialogLabels.getCallback() != null) {
                    dialogLabels.getCallback().onNegativeClick(dialog);
                } else {
                    dialog.dismiss();
                }
            });
        }
        return builder.create();
    }

}
