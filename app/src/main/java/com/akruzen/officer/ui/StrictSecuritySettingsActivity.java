package com.akruzen.officer.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akruzen.officer.R;
import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.material.slider.Slider;

public class StrictSecuritySettingsActivity extends AppCompatActivity {

    Slider durartionSlider;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strict_security_settings);
        // Initialize objects
        tinyDB = new TinyDB(this);
        // Find views by Ids
        durartionSlider = findViewById(R.id.durationSlider);
        // Add listeners
        durartionSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int durationInMillis = (int) slider.getValue() * 1000;
                tinyDB.putInt(TinyDbKeys.COOLDOWN_TIMER_IN_MILLIS, durationInMillis);
            }
        });
        // Method calls
        setViews();
    }

    private void setViews() {
        int durationInSeconds = tinyDB.getInt(TinyDbKeys.COOLDOWN_TIMER_IN_MILLIS) / 1000;
        if (durationInSeconds != 0) durartionSlider.setValue(durationInSeconds);
    }
}