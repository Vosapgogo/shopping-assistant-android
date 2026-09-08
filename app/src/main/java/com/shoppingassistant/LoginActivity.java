package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shoppingassistant.repository.AuthRepository;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText etEmail, etPassword;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository();

        layoutEmail = findViewById(R.id.layout_email_login);
        layoutPassword = findViewById(R.id.layout_password_login);
        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);

        // Setup Back Button
        ImageView btnBack = findViewById(R.id.btn_back_login);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup Sign Up Navigation
        TextView tvGoToRegister = findViewById(R.id.tv_go_to_signup);
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Setup Login Button
        MaterialButton btnLogin = findViewById(R.id.btn_login_submit);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> validateAndLogin());
        }
    }

    private void validateAndLogin() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean isValid = true;

        if (!Pattern.matches(EMAIL_REGEX, email)) {
            layoutEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        if (password.isEmpty()) {
            layoutPassword.setError("Password cannot be empty");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        authRepository.login(email, password, this, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("isLoggedIn", true).apply();

                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                // Only log details in debug builds — release logcat shouldn't
                // carry auth failure details (and definitely never passwords).
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Login error: " + error);
                }
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                layoutEmail.setError("Invalid email or password");
                layoutPassword.setError("Invalid email or password");
            }
        });
    }
}