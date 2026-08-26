package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find the Log In button by its ID
        MaterialButton btnLogin = findViewById(R.id.btn_login);

        // Find the Create Account button by its ID
        MaterialButton btnCreateAccount = findViewById(R.id.btn_create_account);

        // Set click listener for the Log In button
        btnLogin.setOnClickListener(v -> {
            // Create an Intent to navigate to LoginActivity
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Set click listener for the Create Account button
        btnCreateAccount.setOnClickListener(v -> {
            // Create an Intent to navigate to RegistrationActivity
            Intent intent = new Intent(WelcomeActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}