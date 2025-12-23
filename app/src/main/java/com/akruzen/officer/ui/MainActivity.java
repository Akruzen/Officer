package com.akruzen.officer.ui;

import static com.akruzen.officer.constants.TinyDbKeys.IS_CUSTOM_TRIGGER_ENABLED;
import static com.akruzen.officer.constants.TinyDbKeys.IS_MASTER_ENABLED;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.akruzen.officer.CustomTriggerActivity;
import com.akruzen.officer.R;
import com.akruzen.officer.functions.Methods;
import com.akruzen.officer.lib.TinyDB;
import com.akruzen.officer.services.DialogAccessibilityService;
import com.akruzen.officer.views.dialog.DialogLabels;
import com.akruzen.officer.views.dialog.IMaterialDialogActionsCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.akruzen.officer.constants.Links;

public class MainActivity extends AppCompatActivity {

    MaterialSwitch onOffSwitch, strictSecuritySwitch, customTriggerSwitch;
    MaterialCardView permissionsCardView;
    TinyDB tinyDB;
    MaterialButton customTriggerButton;
    TextView versionTextView;

    public void onAccessibilityButtonPress(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    public void onDeviceAdminButtonPress(View view) {
        startActivity(new Intent().setComponent(
                new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));
    }

    public void onUnableToGrantAccessibilityPress(View view) {
        DialogLabels dialogLabels = new DialogLabels();
        dialogLabels.setTitle(getResources().getString(R.string.accessibility_dialog_title))
                .setMessage(getResources().getString(R.string.accessibility_dialog_message))
                .setPositiveText(getResources().getString(R.string.go_to_app_info))
                .setNegativeText(getResources().getString(R.string.dismiss))
                .setCallback(new IMaterialDialogActionsCallback() {
                    @Override
                    public void onPositiveClick(DialogInterface dialogInterface) {
                        IMaterialDialogActionsCallback.super.onPositiveClick(null);
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:com.akruzen.officer"));
                            startActivity(intent);
                        } catch (SecurityException e) {
                            Toast.makeText(MainActivity.this, "Cannot open app info, please open manually", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Methods.getAlertDialog(this, dialogLabels).show();
    }

    public void onConfigureStrictSecurityPress(View view) {
        startActivity(new Intent(this, StrictSecuritySettingsActivity.class));
    }

    public void onSetCustomTriggerPress(View view) {
        startActivity(new Intent(this, CustomTriggerActivity.class));
    }

    public void onContactButtonPress(View view) {
        String uriString = Links.LINKEDIN_LINK;
        if (view.getId() == R.id.githubButton) {
            uriString = Links.GITHUB_LINK;
        } else if (view.getId() == R.id.discordButton) {
            uriString = Links.DISCORD_LINK;
        } else if (view.getId() == R.id.sourcecodeButton) {
            uriString = Links.SOURCE_CODE_LINK;
        }
        // Else default behaviour will be to open linked in
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriString)));
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
        versionTextView = findViewById(R.id.versionTextButton);
        strictSecuritySwitch = findViewById(R.id.strictSecuritySwitch);
        customTriggerButton = findViewById(R.id.setupCustomTriggerButton);
        customTriggerSwitch = findViewById(R.id.customTriggerSwitch);
        // Method Calls
        setVisibilityAndEnablement();
        setSwitchesActions();
        setVersionTextButton();
    }

    private void setVisibilityAndEnablement() {
        boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);

        if (Methods.isAllPermissionsGranted(this)) {
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

        if (Methods.isAdminAccess(this)) {
            findViewById(R.id.deviceAdminButton).setVisibility(View.GONE);
        }
        if (Methods.isAccessibilityServiceEnabled(this, DialogAccessibilityService.class)) {
            findViewById(R.id.accessibilityButton).setVisibility(View.GONE);
        }

        customTriggerSwitch.setChecked(tinyDB.getBoolean(IS_CUSTOM_TRIGGER_ENABLED));
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
        customTriggerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
                tinyDB.putBoolean(IS_CUSTOM_TRIGGER_ENABLED, isChecked);
            }
        });
    }

    private void setVersionTextButton() {
        try {
            PackageInfo packageInfo;
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = getString(R.string.app_name) + " - " + packageInfo.versionName.split("-")[1].trim();
            versionTextView.setText(versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}