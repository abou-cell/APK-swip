package com.abou.autoswipe;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class AutoSwipeService extends AccessibilityService {

    private static final String TINDER_PACKAGE = "com.tinder";
    private static AutoSwipeService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;
    private boolean gestureInProgress = false;
    private long swipeCount = 0;

    private WindowManager windowManager;
    private View overlay;
    private TextView overlayCounter;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            if (!isTinderForeground()) {
                handler.postDelayed(this, 600);
                return;
            }

            if (dismissKnownInterruption()) {
                handler.postDelayed(this, 900);
                return;
            }

            if (!gestureInProgress) {
                performRightSwipe();
            }
        }
    };

    public static AutoSwipeService getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Gesture timing is driven by the loop. Accessibility events keep the
        // active window context current.
    }

    @Override
    public void onInterrupt() {
        stopAutoSwipe();
    }

    @Override
    public void onDestroy() {
        stopAutoSwipe();
        instance = null;
        super.onDestroy();
    }

    public void startAutoSwipe() {
        if (running) return;
        running = true;
        swipeCount = 0;
        showOverlay();
        handler.removeCallbacks(loop);
        handler.postDelayed(loop, 500);
    }

    public void stopAutoSwipe() {
        running = false;
        gestureInProgress = false;
        handler.removeCallbacksAndMessages(null);
        removeOverlay();
    }

    private boolean isTinderForeground() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null) return false;
        return TINDER_PACKAGE.contentEquals(root.getPackageName());
    }

    private void performRightSwipe() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        float startX = width * 0.32f;
        float endX = width * 0.86f;
        float y = height * 0.56f;

        Path path = new Path();
        path.moveTo(startX, y);
        path.lineTo(endX, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 260);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();

        gestureInProgress = true;
        boolean accepted = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                gestureInProgress = false;
                swipeCount++;
                updateCounter();
                if (running) handler.postDelayed(loop, 1050);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                gestureInProgress = false;
                if (running) handler.postDelayed(loop, 1300);
            }
        }, null);

        if (!accepted) {
            gestureInProgress = false;
            handler.postDelayed(loop, 1200);
        }
    }

    private boolean dismissKnownInterruption() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        List<String> labels = Arrays.asList(
                "Keep Swiping",
                "Continue Swiping",
                "Continuer à swiper",
                "Continuer",
                "Seguir deslizando",
                "Weiter swipen"
        );

        for (String label : labels) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(label);
            if (nodes == null) continue;
            for (AccessibilityNodeInfo node : nodes) {
                AccessibilityNodeInfo clickable = node;
                while (clickable != null && !clickable.isClickable()) {
                    clickable = clickable.getParent();
                }
                if (clickable != null && clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showOverlay() {
        if (overlay != null || windowManager == null) return;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(18, 10, 18, 10);
        box.setBackgroundColor(Color.argb(220, 28, 31, 38));

        overlayCounter = new TextView(this);
        overlayCounter.setText("▶ 0");
        overlayCounter.setTextColor(Color.WHITE);
        overlayCounter.setTextSize(15f);
        box.addView(overlayCounter);

        Button stop = new Button(this);
        stop.setText("STOP");
        stop.setOnClickListener(v -> stopAutoSwipe());
        box.addView(stop);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 16;
        params.y = 90;

        overlay = box;
        windowManager.addView(overlay, params);
    }

    private void updateCounter() {
        if (overlayCounter != null) {
            overlayCounter.setText("▶ " + swipeCount);
        }
    }

    private void removeOverlay() {
        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }
        }
        overlay = null;
        overlayCounter = null;
    }
}
