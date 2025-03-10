package com.akruzen.officer.ui;

import static com.akruzen.officer.constants.TinyDbKeys.IS_MASTER_ENABLED;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.akruzen.officer.R;
import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.functions.Methods;
import com.akruzen.officer.lib.TinyDB;
import com.akruzen.officer.services.DialogAccessibilityService;
import com.akruzen.officer.views.dialog.DialogLabels;
import com.akruzen.officer.views.dialog.IMaterialDialogActionsCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.akruzen.officer.constants.Links;

public class MainActivity extends AppCompatActivity {

    MaterialSwitch onOffSwitch;
    MaterialCardView permissionsCardView;
    TinyDB tinyDB;
    MaterialButton versionTextButton;
    ShapeableImageView clipartImageView;

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
        setContentView(R.layout.activity_main);
        // Initialize objects
        tinyDB = new TinyDB(this);
        // Find view by ID
        onOffSwitch = findViewById(R.id.officerSwitch);
        permissionsCardView = findViewById(R.id.permissionsCardView);
        versionTextButton = findViewById(R.id.versionTextButton);
        clipartImageView = findViewById(R.id.clipartImageView);
        // Method Calls
        setVisibilityAndEnablement();
        setOnOffSwitch();
        setVersionTextButton();
    }

    private void setVisibilityAndEnablement() {
        boolean isMasterEnabled = tinyDB.getBoolean(IS_MASTER_ENABLED);

        if (Methods.isAllPermissionsGranted(this)) {
            onOffSwitch.setEnabled(true);
            permissionsCardView.setVisibility(View.GONE);
            onOffSwitch.setChecked(isMasterEnabled);
        } else {
            onOffSwitch.setEnabled(false);
            permissionsCardView.setVisibility(View.VISIBLE);
            onOffSwitch.setChecked(false);
        }

        if (isMasterEnabled) {
            clipartImageView.setImageResource(R.drawable.officer_on);
        } else {
            clipartImageView.setImageResource(R.drawable.officer_off);
        }

        if (tinyDB.getBoolean(TinyDbKeys.IS_ADMIN_ENABLED)) {
            findViewById(R.id.deviceAdminButton).setVisibility(View.GONE);
        }
        if (Methods.isAccessibilityServiceEnabled(this, DialogAccessibilityService.class)) {
            findViewById(R.id.accessibilityButton).setVisibility(View.GONE);
        }
    }

    private void setOnOffSwitch() {
        onOffSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    tinyDB.putBoolean(IS_MASTER_ENABLED, isChecked);
                    if (isChecked) {
                        clipartImageView.setImageResource(R.drawable.officer_on);
                    } else {
                        clipartImageView.setImageResource(R.drawable.officer_off);
                    }
                });
    }

    private void setVersionTextButton() {
        try {
            PackageInfo packageInfo;
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = "Version: " + packageInfo.versionName.split("-")[1].trim();
            versionTextButton.setText(versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}