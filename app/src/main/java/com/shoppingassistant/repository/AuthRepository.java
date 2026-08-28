package com.shoppingassistant.repository;

import com.shoppingassistant.network.ApiClient;
import com.shoppingassistant.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

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
                } else {
                    if (response.code() == 409) {
                        callback.onError("Email already exists (409 Conflict)");
                    } else {
                        callback.onError("Registration failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}