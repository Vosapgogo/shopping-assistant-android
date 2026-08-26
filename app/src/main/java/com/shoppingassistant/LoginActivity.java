package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView; // Make sure this is imported

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find the back button and set click listener
        ImageView btnBack = findViewById(R.id.btn_back_login);
        btnBack.setOnClickListener(v -> finish());

        // Find the "Sign up" text view
        TextView tvGoToSignup = findViewById(R.id.tv_go_to_signup);

        // Set click listener to navigate to RegistrationActivity
        tvGoToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
            // Close current LoginActivity so it doesn't pile up in the back stack
            finish();
        });
    }
}