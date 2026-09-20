package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.shoppingassistant.repository.AuthRepository;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AuthRepository authRepository = new AuthRepository();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // "Logged in" means a valid, non-expired JWT is stored — not a separate flag
            Intent intent;
            if (authRepository.isLoggedIn(SplashActivity.this)) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            }

            startActivity(intent);
            finish();

        }, 2500);
    }
}
