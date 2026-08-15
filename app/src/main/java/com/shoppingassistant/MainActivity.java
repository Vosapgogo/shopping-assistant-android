package com.shoppingassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnCreateList = findViewById(R.id.btn_create_list);
        btnCreateList.setOnClickListener(v -> showCreateListDialog());

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        android.widget.ScrollView contentLists = findViewById(R.id.content_lists);
        android.widget.TextView tvPageTitle = findViewById(R.id.tv_page_title);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_lists) {
                tvPageTitle.setText(R.string.title_my_lists);
                contentLists.setVisibility(View.VISIBLE);
                return true;

            } else if (itemId == R.id.nav_prices) {
                tvPageTitle.setText(R.string.nav_prices);
                contentLists.setVisibility(View.GONE);
                return true;

            } else if (itemId == R.id.nav_map) {
                tvPageTitle.setText(R.string.nav_map);
                contentLists.setVisibility(View.GONE);
                return true;

            } else if (itemId == R.id.nav_profile) {
                tvPageTitle.setText(R.string.nav_profile);
                contentLists.setVisibility(View.GONE);
                return true;
            }

            return false;
        });
    }

    private void showCreateListDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_list, null);
        TextInputEditText etListName = dialogView.findViewById(R.id.et_list_name);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_new_list)
                .setView(dialogView)
                .setPositiveButton(R.string.action_create, (d, which) -> {
                    String listName = etListName.getText().toString().trim();
                    if (!listName.isEmpty()) {
                        String msg = getString(R.string.msg_list_created) + listName;
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, (d, which) -> {
                    d.dismiss();
                })
                .show();

        android.widget.Button btnCreate = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        android.widget.Button btnCancel = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

        int colorGreen = androidx.core.content.ContextCompat.getColor(this, R.color.brand_green);
        int colorGrey = androidx.core.content.ContextCompat.getColor(this, R.color.button_cancel_grey);
        int colorRipple = androidx.core.content.ContextCompat.getColor(this, R.color.brand_green_ripple);

        btnCreate.setTextColor(colorGreen);
        btnCancel.setTextColor(colorGrey);

        if (btnCreate instanceof com.google.android.material.button.MaterialButton) {
            android.content.res.ColorStateList rippleStateList =
                    android.content.res.ColorStateList.valueOf(colorRipple);

            ((com.google.android.material.button.MaterialButton) btnCreate).setRippleColor(rippleStateList);
            ((com.google.android.material.button.MaterialButton) btnCancel).setRippleColor(rippleStateList);
        }
    }
}