package com.akruzen.officer.ui;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akruzen.officer.R;
import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.material.materialswitch.MaterialSwitch;

public class BroadcastEventActivity extends AppCompatActivity {

    MaterialSwitch repeatBroadcastEventsSwitch;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broadcast_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialise objects
        tinyDB = new TinyDB(this);
        // Find views by ID
        repeatBroadcastEventsSwitch = findViewById(R.id.repeatBroadcastEventsSwitch);

        setUpSwitches();
    }

    private void setUpSwitches() {
        repeatBroadcastEventsSwitch.setChecked(tinyDB.getBoolean(TinyDbKeys.IS_REPEAT_BROADCAST_EVENTS_CHECKED));
        repeatBroadcastEventsSwitch.setOnCheckedChangeListener((compoundButton, b) ->
                tinyDB.putBoolean(TinyDbKeys.IS_REPEAT_BROADCAST_EVENTS_CHECKED, b));
    }
}