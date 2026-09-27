package com.shoppingassistant.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.shoppingassistant.R;

/**
 * Screen header with a back arrow and a title, shared by every screen that has a "back".
 *
 * <pre>
 * &lt;com.shoppingassistant.ui.TopBarView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:title="@string/title_create_new_list" /&gt;
 * </pre>
 *
 * The arrow behaves like the system Back button (normally closing the screen); call
 * {@link #setOnBackClickListener} when a screen needs something else.
 */
public class TopBarView extends LinearLayout {

    private final ImageButton backButton;
    private final TextView titleView;

    public TopBarView(Context context) {
        this(context, null);
    }

    public TopBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPaddingRelative(
                getResources().getDimensionPixelSize(R.dimen.top_bar_padding_start),
                dp(8),
                getResources().getDimensionPixelSize(R.dimen.screen_padding_horizontal),
                dp(8));

        LayoutInflater.from(context).inflate(R.layout.view_top_bar, this, true);
        backButton = findViewById(R.id.top_bar_back);
        titleView = findViewById(R.id.top_bar_title);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopBarView);
        titleView.setText(a.getText(R.styleable.TopBarView_title));
        a.recycle();

        backButton.setOnClickListener(v -> {
            Activity activity = findActivity(getContext());
            if (activity instanceof ComponentActivity) {
                ((ComponentActivity) activity).getOnBackPressedDispatcher().onBackPressed();
            } else if (activity != null) {
                activity.finish();
            }
        });
    }

    public void setTitle(CharSequence title) {
        titleView.setText(title);
    }

    public void setOnBackClickListener(OnClickListener listener) {
        backButton.setOnClickListener(listener);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Nullable
    private static Activity findActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
