package com.akruzen.officer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

    public void onShareViaLinkClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Officer App Link", Links.APP_RELEASES_LINK);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link copied!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, Links.APP_RELEASES_LINK);
        startActivity(Intent.createChooser(intent, "Share the app link!"));
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