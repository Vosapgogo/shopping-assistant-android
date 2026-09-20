package com.shoppingassistant.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.shoppingassistant.network.ApiClient;
import com.shoppingassistant.network.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    /** What went wrong, so the UI can react differently (field error vs. toast vs. redirect). */
    public enum ErrorKind {
        NETWORK,
        INVALID_CREDENTIALS,
        EMAIL_TAKEN,
        VALIDATION,
        SERVER,
        /** The account was created, but the automatic login right after it failed. */
        ACCOUNT_CREATED_LOGIN_FAILED
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(ErrorKind kind, String error);
    }

    public void register(String name, String email, String password, Context context, AuthCallback callback) {
        ApiService.RegisterRequest request = new ApiService.RegisterRequest(name, email, password);

        apiService.register(request).enqueue(new Callback<ApiService.RegisterResponse>() {
            @Override
            public void onResponse(Call<ApiService.RegisterResponse> call, Response<ApiService.RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // /register returns no token. Log in right away, otherwise the user
                    // would be treated as logged in while having no JWT to call the API with.
                    login(email, password, context, new AuthCallback() {
                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess("Account created successfully!");
                        }

                        @Override
                        public void onError(ErrorKind kind, String error) {
                            callback.onError(ErrorKind.ACCOUNT_CREATED_LOGIN_FAILED,
                                    "Account created, but automatic login failed. Please log in.");
                        }
                    });
                } else if (response.code() == 409) {
                    callback.onError(ErrorKind.EMAIL_TAKEN, "This email is already registered");
                } else if (response.code() == 400) {
                    callback.onError(ErrorKind.VALIDATION, "Please check the entered data and try again");
                } else {
                    callback.onError(ErrorKind.SERVER, "Something went wrong on our side. Please try again later");
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                callback.onError(ErrorKind.NETWORK, "Can't reach the server. Check your internet connection and try again");
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
                        callback.onError(ErrorKind.SERVER, "Could not store your session securely on this device");
                    }
                } else if (response.code() == 401) {
                    callback.onError(ErrorKind.INVALID_CREDENTIALS, "Invalid email or password");
                } else if (response.code() == 400) {
                    callback.onError(ErrorKind.VALIDATION, "Please check your email and password");
                } else {
                    callback.onError(ErrorKind.SERVER, "Something went wrong on our side. Please try again later");
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                callback.onError(ErrorKind.NETWORK, "Can't reach the server. Check your internet connection and try again");
            }
        });
    }

    /**
     * Single source of truth for "is the user logged in": a JWT is stored and has not
     * expired yet. An expired token is removed on the spot.
     */
    public boolean isLoggedIn(Context context) {
        String token = getToken(context);
        if (token == null) {
            return false;
        }
        if (isExpired(token)) {
            clearToken(context);
            return false;
        }
        return true;
    }

    /** Reads the "exp" claim (seconds since epoch) from the JWT payload. Unreadable or missing counts as expired. */
    private static boolean isExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return true;
            }
            byte[] payload = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            long exp = new JSONObject(new String(payload, StandardCharsets.UTF_8)).optLong("exp", 0);
            return exp == 0 || exp * 1000 <= System.currentTimeMillis();
        } catch (IllegalArgumentException | JSONException e) {
            return true;
        }
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
        Context appContext = context.getApplicationContext();

        // androidx.security.crypto.MasterKeys is deprecated; MasterKey.Builder is the replacement.
        MasterKey masterKey = new MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                appContext,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
