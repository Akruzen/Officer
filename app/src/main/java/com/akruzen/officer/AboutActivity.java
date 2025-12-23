package com.akruzen.officer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akruzen.officer.constants.Links;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AboutActivity extends AppCompatActivity {

    public void onContactButtonPress(View view) {
        String uriString = Links.LINKEDIN_LINK;
        if (view.getId() == R.id.githubButton) {
            uriString = Links.GITHUB_LINK;
        } else if (view.getId() == R.id.discordButton) {
            uriString = Links.DISCORD_LINK;
        } else if (view.getId() == R.id.sourceCodeButton) {
            uriString = Links.SOURCE_CODE_LINK;
        }
        // Else default behaviour will be to open linked in
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriString)));
    }

    public void openSourceLicencesTapped(View view) {
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    public void changeLogPressed(View view) {
        showChangeLogBottomSheet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showChangeLogBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.changelog_bottom_sheet);
        bottomSheetDialog.show();
    }
}