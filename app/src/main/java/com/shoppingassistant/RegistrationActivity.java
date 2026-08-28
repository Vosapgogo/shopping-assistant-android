package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shoppingassistant.repository.AuthRepository;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private static final String NAME_REGEX = "^[\\p{L}]+[\\s]+[\\p{L}]+.*$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    private TextInputLayout layoutName, layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authRepository = new AuthRepository();

        layoutName = findViewById(R.id.layout_name);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        ImageView btnBack = findViewById(R.id.btn_back_register);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView tvGoToLogin = findViewById(R.id.tv_go_to_login);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        MaterialButton btnRegister = findViewById(R.id.btn_register_submit);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> validateAndRegister());
        }
    }

    private void validateAndRegister() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean isValid = true;

        if (!Pattern.matches(NAME_REGEX, name)) {
            layoutName.setError("Please enter your full name (e.g. John Doe)");
            isValid = false;
        }

        if (!Pattern.matches(EMAIL_REGEX, email)) {
            layoutEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        if (!Pattern.matches(PASSWORD_REGEX, password)) {
            layoutPassword.setError("Password must be at least 8 chars, with 1 uppercase, 1 lowercase, and 1 number");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (isValid) {
            // Send data to backend repository, now including the name parameter
            authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RegistrationActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}