package com.shoppingassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.shoppingassistant.repository.AuthRepository;

public class ProfileActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authRepository = new AuthRepository();

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        // 1. Access SharedPreferences to clear the login state
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", false)
                .remove("userName")
                .remove("userEmail")
                .apply();

        // 2. Clear the encrypted JWT too — previously it stayed in storage
        //    after "logging out", so it would silently outlive the session.
        authRepository.clearToken(this);

        // 3. Show a quick confirmation message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // 4. Navigate back to WelcomeActivity, clearing the back stack so the
        //    hardware back button can't return to the logged-in screens.
        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}