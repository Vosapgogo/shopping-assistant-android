package com.shoppingassistant.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.shoppingassistant.R;

/**
 * Header of the main (bottom navigation) screens: a big page title and a notifications bell.
 *
 * <pre>
 * &lt;com.shoppingassistant.ui.PageHeaderView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:title="@string/title_my_lists" /&gt;
 * </pre>
 *
 * Screens that switch tabs in place can change the title with {@link #setTitle(int)}.
 */
public class PageHeaderView extends LinearLayout {

    private final TextView titleView;
    private final ImageButton notificationsButton;

    public PageHeaderView(Context context) {
        this(context, null);
    }

    public PageHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPaddingRelative(
                getResources().getDimensionPixelSize(R.dimen.screen_padding_horizontal),
                dp(16),
                getResources().getDimensionPixelSize(R.dimen.page_header_padding_end),
                dp(16));

        LayoutInflater.from(context).inflate(R.layout.view_page_header, this, true);
        titleView = findViewById(R.id.page_header_title);
        notificationsButton = findViewById(R.id.page_header_notifications);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageHeaderView);
        titleView.setText(a.getText(R.styleable.PageHeaderView_title));
        a.recycle();
    }

    public void setTitle(CharSequence title) {
        titleView.setText(title);
    }

    public void setTitle(@StringRes int titleRes) {
        titleView.setText(titleRes);
    }

    public void setOnNotificationsClickListener(OnClickListener listener) {
        notificationsButton.setOnClickListener(listener);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
