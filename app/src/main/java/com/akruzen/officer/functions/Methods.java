package com.akruzen.officer.functions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.akruzen.officer.services.ScreenStateService;
import com.akruzen.officer.views.dialog.DialogLabels;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Methods {

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

    public static boolean isScreenStateServiceActive(Context context) {
        ComponentName component = new ComponentName(context, ScreenStateService.class);
        int status = context.getPackageManager().getComponentEnabledSetting(component);
        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return true;
        } else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            return false;
        }
        return false;
    }

    public static void setScreenStateService(Context context, boolean enabled) {
        ComponentName component = new ComponentName(context, ScreenStateService.class);
        if (enabled) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
            context.startForegroundService(new Intent(context, ScreenStateService.class));
        } else {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
        }
    }

    public static void dismissKeyboard(Activity activity, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = activity.getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
    }

}
