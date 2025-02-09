package com.akruzen.officer;

import static com.akruzen.officer.Constants.TinyDbKeys.IS_ADMIN_ENABLED;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.akruzen.officer.lib.TinyDB;

public class DeviceAdmin extends DeviceAdminReceiver {

    TinyDB tinyDB;
    @Override
    public void onEnabled (@NonNull Context context , @NonNull Intent intent) {
        super.onEnabled(context , intent) ;
        tinyDB = new TinyDB(context);
        tinyDB.putBoolean(IS_ADMIN_ENABLED, true);
    }
    @Override
    public void onDisabled (@NonNull Context context , @NonNull Intent intent) {
        super.onDisabled(context , intent) ;
        tinyDB.putBoolean(IS_ADMIN_ENABLED, false);
    }
}
