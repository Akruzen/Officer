package com.akruzen.officer;

import static com.akruzen.officer.Constants.TinyDbKeys.IS_MASTER_ENABLED;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.akruzen.officer.Functions.Methods;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.akruzen.officer.Constants.Strings;

public class MainActivity extends AppCompatActivity {

    MaterialSwitch onOffSwitch;
    MaterialCardView permissionsCardView;
    TinyDB tinyDB;
    MaterialButton versionTextButton;

    public void onAccessibilityButtonPress(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    public void onDeviceAdminButtonPress(View view) {
        startActivity(new Intent().setComponent(
                new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));
    }

    public void onContactButtonPress(View view) {
        String uriString = Strings.LINKEDIN_LINK;
        if (view.getId() == R.id.githubButton) {
            uriString = Strings.GITHUB_LINK;
        } else if (view.getId() == R.id.discordButton) {
            uriString = Strings.DISCORD_LINK;
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
        // Method Calls
        setVisibilityAndEnablement();
        setOnOffSwitch();
        setVersionTextButton();
    }

    private void setVisibilityAndEnablement() {
        if (Methods.isAllPermissionsGranted(this)) {
            onOffSwitch.setEnabled(true);
            permissionsCardView.setVisibility(View.GONE);
            onOffSwitch.setChecked(tinyDB.getBoolean(IS_MASTER_ENABLED));
        } else {
            onOffSwitch.setEnabled(false);
            permissionsCardView.setVisibility(View.VISIBLE);
            onOffSwitch.setChecked(false);
        }
    }

    private void setOnOffSwitch() {
        onOffSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> tinyDB.putBoolean(IS_MASTER_ENABLED, isChecked));
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