package com.shoppingassistant;

import android.content.Intent;
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
        // 1. Clear the encrypted JWT — it is the only login state there is, so removing
        //    it logs the user out (and it no longer outlives the session).
        authRepository.clearToken(this);

        // 2. Show a quick confirmation message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // 3. Navigate back to WelcomeActivity, clearing the back stack so the
        //    hardware back button can't return to the logged-in screens.
        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
