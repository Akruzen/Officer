package com.akruzen.officer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akruzen.officer.constants.TinyDbKeys;
import com.akruzen.officer.functions.Methods;
import com.akruzen.officer.functions.PermissionHelper;
import com.akruzen.officer.lib.TinyDB;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;


public class SmsAlertActivity extends AppCompatActivity {

    MaterialSwitch locationSwitch;
    TinyDB tinyDB;
    MaterialButton sim1Button, sim2Button;
    MaterialButtonToggleGroup simToggleGroup;
    TextInputEditText phoneNumberEditText, smsMessageEditText;
    Slider ignoreCountSlider, cooldownCountSlider;
    TextView previewTextView;
    ExtendedFloatingActionButton saveFab;

    private final ActivityResultLauncher<String> phoneStatePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setSims();
                } else {
                    Snackbar.make(simToggleGroup, getString(R.string.sim_permission_required), Snackbar.LENGTH_LONG).show();
                }
                updateSaveButtonState();
            }
    );

    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    locationSwitch.setChecked(false);
                }
                tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, isGranted);
                updateSaveButtonState();
                updatePreview();
            }
    );

    private final ActivityResultLauncher<String[]> foregroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    } else {
                        tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, true);
                        updateSaveButtonState();
                        updatePreview();
                    }
                } else {
                    locationSwitch.setChecked(false);
                    tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, false);
                    updateSaveButtonState();
                    updatePreview();
                }
            }
    );

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Methods.dismissKeyboard(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms_alert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialise objects
        tinyDB = new TinyDB(this);

        // Find view by ID
        locationSwitch = findViewById(R.id.locationSwitch);
        simToggleGroup = findViewById(R.id.simToggleButtonGroup);
        sim1Button = findViewById(R.id.sim1Button);
        sim2Button = findViewById(R.id.sim2Button);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        smsMessageEditText = findViewById(R.id.smsMessageEditText);
        ignoreCountSlider = findViewById(R.id.ignoreCountSlider);
        cooldownCountSlider = findViewById(R.id.cooldownCountSlider);
        previewTextView = findViewById(R.id.previewTextView);
        saveFab = findViewById(R.id.saveFab2);

        prepopulate();
        setListeners();
        setSims();
        updatePreview();
        updateSaveButtonState();
    }

    private void prepopulate() {
        phoneNumberEditText.setText(tinyDB.getString(TinyDbKeys.SMS_PHONE_NUMBER));
        smsMessageEditText.setText(tinyDB.getString(TinyDbKeys.SMS_TEXT_MESSAGE));
        ignoreCountSlider.setValue(tinyDB.getInt(TinyDbKeys.SMS_ALERT_IGNORE_COUNT));
        int cooldownCount = tinyDB.getInt(TinyDbKeys.SMS_ALERT_COOLDOWN_COUNT);
        cooldownCountSlider.setValue(cooldownCount == 0 ? -1 : cooldownCount);
        locationSwitch.setChecked(tinyDB.getBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED));
        int selectedSim = tinyDB.getInt(TinyDbKeys.SELECTED_SIM);
        if (selectedSim == 1) {
            simToggleGroup.check(R.id.sim2Button);
        } else {
            simToggleGroup.check(R.id.sim1Button);
        }
    }

    private void setListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePreview();
                updateSaveButtonState();
            }
        };
        phoneNumberEditText.addTextChangedListener(watcher);
        smsMessageEditText.addTextChangedListener(watcher);

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionHelper.isAlwaysFineLocationGranted(this)) {
                    Snackbar.make(locationSwitch, getString(R.string.location_permission_required), Snackbar.LENGTH_SHORT).show();
                    foregroundLocationPermissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                } else {
                    tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, true);
                }
            } else {
                tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, false);
            }
            updatePreview();
            updateSaveButtonState();
        });

        ignoreCountSlider.addOnChangeListener((slider, value, fromUser) -> updateSaveButtonState());
        cooldownCountSlider.addOnChangeListener((slider, value, fromUser) -> updateSaveButtonState());
        simToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> updateSaveButtonState());

        saveFab.setOnClickListener(v -> {
            tinyDB.putString(TinyDbKeys.SMS_PHONE_NUMBER, Objects.requireNonNull(phoneNumberEditText.getText()).toString());
            tinyDB.putString(TinyDbKeys.SMS_TEXT_MESSAGE, Objects.requireNonNull(smsMessageEditText.getText()).toString());
            tinyDB.putInt(TinyDbKeys.SMS_ALERT_IGNORE_COUNT, (int) ignoreCountSlider.getValue());
            tinyDB.putInt(TinyDbKeys.SMS_ALERT_COOLDOWN_COUNT, (int) cooldownCountSlider.getValue());
            tinyDB.putBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED, locationSwitch.isChecked());
            tinyDB.putInt(TinyDbKeys.SELECTED_SIM, simToggleGroup.getCheckedButtonId() == R.id.sim2Button ? 1 : 0);
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updatePreview() {
        String message = Objects.requireNonNull(smsMessageEditText.getText()).toString();
        if (message.isEmpty()) {
            previewTextView.setText(getString(R.string.preview_placeholder));
        } else {
            if (locationSwitch.isChecked()) {
                message += getString(R.string.maps_link_template);
            }
            previewTextView.setText(message);
        }
    }

    private void updateSaveButtonState() {
        String phone = Objects.requireNonNull(phoneNumberEditText.getText()).toString();
        String message = Objects.requireNonNull(smsMessageEditText.getText()).toString();
        boolean isPhoneValid = phone.matches("^\\+[1-9]\\d{1,14}$");
        boolean isMessageNotEmpty = !message.isEmpty();
        boolean hasPhoneStatePermission = PermissionHelper.isReadPhoneStatePermissionGranted(this);
        boolean hasLocationPermission = !locationSwitch.isChecked() || PermissionHelper.isAlwaysFineLocationGranted(this);

        boolean isValid = isPhoneValid && isMessageNotEmpty && hasPhoneStatePermission && hasLocationPermission;

        // Check if data has changed
        boolean hasChanged = !phone.equals(tinyDB.getString(TinyDbKeys.SMS_PHONE_NUMBER)) ||
                !message.equals(tinyDB.getString(TinyDbKeys.SMS_TEXT_MESSAGE)) ||
                (int) ignoreCountSlider.getValue() != tinyDB.getInt(TinyDbKeys.SMS_ALERT_IGNORE_COUNT) ||
                (int) cooldownCountSlider.getValue() != tinyDB.getInt(TinyDbKeys.SMS_ALERT_COOLDOWN_COUNT) ||
                locationSwitch.isChecked() != tinyDB.getBoolean(TinyDbKeys.IS_ALERT_LOCATION_APPEND_CHECKED) ||
                (simToggleGroup.getCheckedButtonId() == R.id.sim2Button ? 1 : 0) != tinyDB.getInt(TinyDbKeys.SELECTED_SIM);

        saveFab.setEnabled(isValid && hasChanged);
    }

    @SuppressLint("MissingPermission")
    private void setSims() {
        if (!PermissionHelper.isReadPhoneStatePermissionGranted(this)) {
            phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);
            return;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (subscriptionManager != null) {
            int maxSimSlots = subscriptionManager.getActiveSubscriptionInfoCountMax();
            List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();

            if (maxSimSlots < 2) {
                sim2Button.setEnabled(false);
            }

            boolean sim1Active = false;
            boolean sim2Active = false;

            if (activeSubscriptions != null) {
                for (SubscriptionInfo info : activeSubscriptions) {
                    if (info.getSimSlotIndex() == 0) {
                        sim1Active = true;
                        sim1Button.setText(String.format("%s (1)", info.getDisplayName()));
                    } else if (info.getSimSlotIndex() == 1) {
                        sim2Active = true;
                        sim2Button.setText(String.format("%s (2)", info.getDisplayName()));
                    }
                }
            }

            if (!sim1Active) {
                sim1Button.setEnabled(false);
            }
            if (!sim2Active) {
                sim2Button.setEnabled(false);
            }
        }
    }
}