package com.abou.autoswipe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 64, 48, 48);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(248, 249, 251));

        TextView title = new TextView(this);
        title.setText("AUTO SWIPE");
        title.setTextSize(28f);
        title.setTextColor(Color.rgb(28, 31, 38));
        title.setGravity(Gravity.CENTER);
        root.addView(title, fullWidth(80));

        TextView info = new TextView(this);
        info.setText("1. Enable the accessibility service once.\n2. Configure your preferences in Tinder yourself.\n3. Press START. Tinder opens and the app swipes right until you press STOP.");
        info.setTextSize(17f);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, 30, 0, 30);
        root.addView(info, fullWidth(ViewGroup.LayoutParams.WRAP_CONTENT));

        status = new TextView(this);
        status.setTextSize(18f);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 15, 0, 30);
        root.addView(status, fullWidth(70));

        Button accessibility = new Button(this);
        accessibility.setText("ENABLE ACCESSIBILITY SERVICE");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(accessibility, fullWidth(90));

        Button start = new Button(this);
        start.setText("START SWIPING");
        start.setTextSize(20f);
        start.setOnClickListener(v -> startSwiping());
        root.addView(start, fullWidth(105));

        Button stop = new Button(this);
        stop.setText("STOP");
        stop.setTextSize(20f);
        stop.setOnClickListener(v -> {
            AutoSwipeService service = AutoSwipeService.getInstance();
            if (service != null) service.stopAutoSwipe();
            refreshStatus();
        });
        root.addView(stop, fullWidth(105));

        TextView note = new TextView(this);
        note.setText("The app has no Internet permission. It only sends accessibility gestures while Tinder is the foreground app. Keep the screen on during a session.");
        note.setTextSize(14f);
        note.setTextColor(Color.GRAY);
        note.setPadding(0, 30, 0, 0);
        root.addView(note, fullWidth(ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void startSwiping() {
        AutoSwipeService service = AutoSwipeService.getInstance();
        if (service == null) {
            Toast.makeText(this, "Enable Auto Swipe in Accessibility settings first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }

        service.startAutoSwipe();
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.tinder");
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } else {
            Toast.makeText(this, "Tinder app was not found. Open Tinder manually.", Toast.LENGTH_LONG).show();
        }
        refreshStatus();
    }

    private void refreshStatus() {
        AutoSwipeService service = AutoSwipeService.getInstance();
        if (service == null) {
            status.setText("Accessibility service: OFF");
            status.setTextColor(Color.rgb(180, 70, 70));
        } else if (service.isRunning()) {
            status.setText("SWIPING: ON");
            status.setTextColor(Color.rgb(30, 145, 85));
        } else {
            status.setText("Ready — press START");
            status.setTextColor(Color.rgb(70, 90, 130));
        }
    }

    private LinearLayout.LayoutParams fullWidth(int height) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
        );
        p.setMargins(0, 10, 0, 10);
        return p;
    }
}
