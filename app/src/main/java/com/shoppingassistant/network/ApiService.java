package com.shoppingassistant.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // DTO classes for request and response
    class RegisterRequest {
        public String name; // Added name field
        public String email;
        public String password;

        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }
    class RegisterResponse {
        public Long id;
        public String name;
        public String email;
    }
}