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

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    // Regex for Full Name: At least two words separated by a space
    private static final String NAME_REGEX = "^[\\p{L}]+[\\s]+[\\p{L}]+.*$";

    // Regex for Email: Standard email format
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Regex for Password: Min 8 chars, at least 1 uppercase, 1 lowercase, 1 number
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    private TextInputLayout layoutName, layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        layoutName = findViewById(R.id.layout_name);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Setup Back Button
        ImageView btnBack = findViewById(R.id.btn_back_register);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup Login Navigation
        TextView tvGoToLogin = findViewById(R.id.tv_go_to_login);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Setup Create Account Button
        MaterialButton btnRegister = findViewById(R.id.btn_register_submit);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> validateAndRegister());
        }
    }

    private void validateAndRegister() {
        // Reset previous errors
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        // Get text from inputs
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean isValid = true;

        // Validate Full Name
        if (!Pattern.matches(NAME_REGEX, name)) {
            layoutName.setError("Please enter your full name (e.g. John Doe)");
            isValid = false;
        }

        // Validate Email
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            layoutEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate Password
        if (!Pattern.matches(PASSWORD_REGEX, password)) {
            layoutPassword.setError("Password must be at least 8 chars, with 1 uppercase, 1 lowercase, and 1 number");
            isValid = false;
        }

        // Validate Confirm Password
        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        // If all fields are valid, proceed with registration
        if (isValid) {
            // TODO: Connect to backend or Room database to save the user
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            // Redirect to Login or Main Activity after successful registration
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}