package com.akruzen.officer.ui;

import static com.akruzen.officer.constants.TinyDbKeys.IS_CUSTOM_TRIGGER_ENABLED;
import static com.akruzen.officer.constants.TinyDbKeys.IS_MASTER_ENABLED;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.akruzen.officer.AboutActivity;
import com.akruzen.officer.CustomTriggerActivity;
import com.akruzen.officer.R;
import com.akruzen.officer.SmsAlertActivity;
import com.akruzen.officer.WelcomeActivity;
import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.functions.Methods;
import com.akruzen.officer.functions.PermissionHelper;
import com.akruzen.officer.lib.TinyDB;
import com.akruzen.officer.services.DialogAccessibilityService;
import com.akruzen.officer.views.dialog.DialogLabels;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity {

    MaterialSwitch onOffSwitch, strictSecuritySwitch, customTriggerSwitch, smsAlertSwitch, broadcastEventSwitch;
    MaterialCardView permissionsCardView;
    TinyDB tinyDB;
    MaterialButton customTriggerButton;

    public void onAccessibilityButtonPress(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    public void onAboutButtonPress(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onDeviceAdminButtonPress(View view) {
        startActivity(new Intent().setComponent(
                new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));
    }

    public void onConfigureSmsAlertPressed(View view) {
        // Ask for Sms Permission
        if (PermissionHelper.isSmsPermissionGranted(this)) {
            startActivity(new Intent(this, SmsAlertActivity.class));
        } else {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 101);
        }
    }

    public void onBroadcastEventPressed(View view) {
        startActivity(new Intent(this, BroadcastEventActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (PermissionHelper.isSmsPermissionGranted(this)) {
                startActivity(new Intent(this, SmsAlertActivity.class));
            }
        }
    }

    public void onUnableToGrantAccessibilityPress(View view) {
        DialogLabels dialogLabels = new DialogLabels();
        dialogLabels.setTitle(getResources().getString(R.string.accessibility_dialog_title))
                .setMessage(getResources().getString(R.string.accessibility_dialog_message))
                .setNegativeText(getResources().getString(R.string.dismiss));
        Methods.getAlertDialog(this, dialogLabels).show();
    }

    public void onConfigureStrictSecurityPress(View view) {
        startActivity(new Intent(this, StrictSecuritySettingsActivity.class));
    }

    public void onSetCustomTriggerPress(View view) {
        startActivity(new Intent(this, CustomTriggerActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVisibilityAndEnablement();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleLinearLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize objects
        tinyDB = new TinyDB(this);
        // Find view by ID
        onOffSwitch = findViewById(R.id.officerSwitch);
        permissionsCardView = findViewById(R.id.permissionsCardView);
        strictSecuritySwitch = findViewById(R.id.strictSecuritySwitch);
        customTriggerButton = findViewById(R.id.setupCustomTriggerButton);
        customTriggerSwitch = findViewById(R.id.customTriggerSwitch);
        smsAlertSwitch = findViewById(R.id.smsAlertSwitch);
        broadcastEventSwitch = findViewById(R.id.broadcastEventSwitch);
        // Method Calls
        setVisibilityAndEnablement();
        setSwitchesActions();

        if (!tinyDB.getBoolean(TinyDbKeys.IS_OLD_USER)) {
            startActivity(new Intent(this, WelcomeActivity.class));
            tinyDB.putBoolean(TinyDbKeys.IS_OLD_USER, true);
        }
    }

    private void setVisibilityAndEnablement() {
        boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);

        if (PermissionHelper.isAllMandatoryPermissionsGranted(this)) {
            onOffSwitch.setEnabled(true);
            permissionsCardView.setVisibility(View.GONE);
            onOffSwitch.setChecked(isMasterEnabled);
            customTriggerButton.setEnabled(true);
            customTriggerSwitch.setEnabled(true);
        } else {
            onOffSwitch.setEnabled(false);
            permissionsCardView.setVisibility(View.VISIBLE);
            onOffSwitch.setChecked(false);
            customTriggerButton.setEnabled(false);
            customTriggerSwitch.setEnabled(false);
        }

        if (Methods.isScreenStateServiceActive(this)) {
            strictSecuritySwitch.setChecked(true);
        }

        if (PermissionHelper.isAdminAccess(this)) {
            findViewById(R.id.deviceAdminButton).setVisibility(View.GONE);
        }
        if (PermissionHelper.isAccessibilityServiceEnabled(this, DialogAccessibilityService.class)) {
            findViewById(R.id.accessibilityButton).setVisibility(View.GONE);
        }
        if (PermissionHelper.isSmsPermissionGranted(this)) {
            smsAlertSwitch.setEnabled(true);
            smsAlertSwitch.setChecked(tinyDB.getBoolean(TinyDbKeys.IS_SMS_ALERT_ENABLED));
        } else {
            smsAlertSwitch.setChecked(false);
            smsAlertSwitch.setEnabled(false);
            tinyDB.putBoolean(TinyDbKeys.IS_SMS_ALERT_ENABLED, false);
        }

        customTriggerSwitch.setChecked(tinyDB.getBoolean(IS_CUSTOM_TRIGGER_ENABLED));
        broadcastEventSwitch.setChecked(tinyDB.getBoolean(TinyDbKeys.IS_BROADCAST_EVENT_ENABLED));
    }

    private void setSwitchesActions() {
        onOffSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> tinyDB.putBoolean(IS_MASTER_ENABLED, isChecked));
        strictSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!Methods.isScreenStateServiceActive(this)) {
                    Methods.setScreenStateService(this, true);
                }
            } else {
                if (Methods.isScreenStateServiceActive(this)) {
                    Methods.setScreenStateService(this, false);
                }
            }
        });
        customTriggerSwitch.setOnCheckedChangeListener((compoundButton, isChecked) ->
                tinyDB.putBoolean(IS_CUSTOM_TRIGGER_ENABLED, isChecked));
        smsAlertSwitch.setOnCheckedChangeListener((compoundButton, isChecked) ->
                tinyDB.putBoolean(TinyDbKeys.IS_SMS_ALERT_ENABLED, isChecked));
        broadcastEventSwitch.setOnCheckedChangeListener((compoundButton, isChecked) ->
                tinyDB.putBoolean(TinyDbKeys.IS_BROADCAST_EVENT_ENABLED, isChecked));
    }
}