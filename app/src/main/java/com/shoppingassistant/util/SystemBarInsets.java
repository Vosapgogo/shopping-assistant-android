package com.shoppingassistant.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * With targetSdk 35 every screen is drawn edge-to-edge: content goes under the status bar
 * (which then swallows taps on e.g. a back arrow) and under the gesture bar and keyboard.
 */
public final class SystemBarInsets {

    private SystemBarInsets() {
    }

    /**
     * Adds the system bar sizes to the view's own padding.
     *
     * @param padBottom also keep the content above the navigation bar and the keyboard; pass
     *                  false on screens with a BottomNavigationView, which pads itself
     */
    public static void apply(View root, boolean padBottom) {
        int left = root.getPaddingLeft();
        int top = root.getPaddingTop();
        int right = root.getPaddingRight();
        int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int bottomInset = padBottom
                    ? Math.max(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)
                    : 0;

            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bottomInset);
            return insets;
        });
    }
}
