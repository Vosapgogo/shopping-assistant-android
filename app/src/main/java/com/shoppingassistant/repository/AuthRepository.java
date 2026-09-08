package com.shoppingassistant.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.shoppingassistant.network.ApiClient;
import com.shoppingassistant.network.ApiService;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String PREFS_FILE = "secure_prefs";
    private static final String KEY_JWT = "jwt_token";

    private final ApiService apiService;

    public AuthRepository() {
        this.apiService = ApiClient.getApiService();
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        ApiService.RegisterRequest request = new ApiService.RegisterRequest(name, email, password);

        apiService.register(request).enqueue(new Callback<ApiService.RegisterResponse>() {
            @Override
            public void onResponse(Call<ApiService.RegisterResponse> call, Response<ApiService.RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("Registration successful!");
                } else if (response.code() == 409) {
                    callback.onError("Email already exists (409 Conflict)");
                } else {
                    callback.onError("Registration failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void login(String email, String password, Context context, AuthCallback callback) {
        ApiService.LoginRequest request = new ApiService.LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call, Response<ApiService.LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().token;
                    try {
                        saveToken(context, token);
                        callback.onSuccess("Login successful!");
                    } catch (GeneralSecurityException | IOException e) {
                        callback.onError("Security error: " + e.getMessage());
                    }
                } else {
                    callback.onError("Invalid email or password (401)");
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /** Persists the JWT in encrypted storage. */
    private void saveToken(Context context, String token) throws GeneralSecurityException, IOException {
        getEncryptedPrefs(context).edit().putString(KEY_JWT, token).apply();
    }

    /** Reads the stored JWT, e.g. to attach as "Authorization: Bearer <token>" on future protected calls. Null if not logged in. */
    public String getToken(Context context) {
        try {
            return getEncryptedPrefs(context).getString(KEY_JWT, null);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }

    /**
     * Removes the stored JWT. Call this on logout — previously the token was
     * never cleared, so it stayed in encrypted storage (and usable) even
     * after "logging out" locally.
     */
    public void clearToken(Context context) {
        try {
            getEncryptedPrefs(context).edit().remove(KEY_JWT).apply();
        } catch (GeneralSecurityException | IOException e) {
            // Nothing readable to clear — safe to ignore during logout.
        }
    }

    private SharedPreferences getEncryptedPrefs(Context context) throws GeneralSecurityException, IOException {
        // androidx.security.crypto.MasterKeys is deprecated; MasterKey.Builder is the replacement.
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}