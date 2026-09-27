package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.shoppingassistant.repository.AuthRepository;
import com.shoppingassistant.util.SystemBarInsets;

public class MainActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.apply(findViewById(R.id.root), false);

        authRepository = new AuthRepository();

        MaterialButton btnCreateList = findViewById(R.id.btn_create_list);
        btnCreateList.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateListActivity.class)));

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
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

                return false;
            }

            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // The token may have expired while the app sat in the background
        if (!authRepository.isLoggedIn(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
