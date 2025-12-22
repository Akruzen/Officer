package com.akruzen.officer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

public class CustomTriggerActivity extends AppCompatActivity {

    EventBroadcastReceiver receiver;
    private static final int MAX_ITEMS = 10;
    private final LinkedHashSet<String> classNames = new LinkedHashSet<>();
    LinearLayout eventContainer;
    private MaterialRadioButton selectedRadio = null;
    private String selectedClassName = null;
    private String currentClassName = null;
    TinyDB tinyDB = null;
    private TextView currentSelectedTV = null;
    ExtendedFloatingActionButton saveFab = null;

    public void saveClicked(View view) {
        if (selectedClassName != null && !selectedClassName.isEmpty()) {
            tinyDB.putString(TinyDbKeys.CUSTOM_TRIGGER, selectedClassName);
            Toast.makeText(this, "Custom trigger saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please select an event", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custom_trigger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find views by ID
        eventContainer = findViewById(R.id.eventLinearLayout);
        currentSelectedTV = findViewById(R.id.currentTriggerTV);
        saveFab = findViewById(R.id.saveFab);

        // Object creation
        receiver = new EventBroadcastReceiver();
        tinyDB = new TinyDB(this);
        currentClassName = tinyDB.getString(TinyDbKeys.CUSTOM_TRIGGER);
        currentClassName = getString(R.string.current_trigger) + (currentClassName.isEmpty() ? " No custom trigger set" : " " + currentClassName);
        currentSelectedTV.setText(currentClassName);

        // Register receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, new IntentFilter("com.akruzen.officer.SYSTEM_UI_EVENT"), RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, new IntentFilter("com.akruzen.officer.SYSTEM_UI_EVENT"));
        }
    }

    private void onNewEventReceived(String className) {
        // If already present, skip
        if (classNames.contains(className)) {
            return;
        }
        classNames.add(className);
        // Evict oldest if size > 10
        if (classNames.size() > MAX_ITEMS) {
            Iterator<String> it = classNames.iterator();
            it.next();
            it.remove();
        }
        renderList();
    }

    private void renderList() {
        eventContainer.removeAllViews();
        for (String name : classNames) {
            eventContainer.addView(createRow(name));
        }
    }

    private View createRow(String className) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 8, 16, 8);

        MaterialRadioButton radio = new MaterialRadioButton(this);
        TextView text = new TextView(this);

        text.setText(className);
        text.setTextSize(14f);
        text.setPadding(16, 0, 0, 0);

        radio.setOnClickListener(v -> {
            if (selectedRadio != null) {
                selectedRadio.setChecked(false);
            }
            selectedRadio = radio;
            selectedClassName = className;
            radio.setChecked(true);
            saveFab.setEnabled(true);
        });

        // Restore selection after re-render
        if (className.equals(selectedClassName)) {
            radio.setChecked(true);
            selectedRadio = radio;
        }

        row.addView(radio);
        row.addView(text);

        return row;
    }

    private class EventBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "com.akruzen.officer.SYSTEM_UI_EVENT")) {
                String fullClassName = intent.getStringExtra("eventClassName");
                if (fullClassName != null) {
                    String className = fullClassName.replaceFirst("^com\\.android\\.systemui\\.", "");
                    runOnUiThread(() -> onNewEventReceived(className));
                }
            }
        }
    }
}