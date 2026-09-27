// ApiClient.java
package com.shoppingassistant.network;

import android.content.Context;

import com.shoppingassistant.BuildConfig;
import com.shoppingassistant.repository.AuthRepository;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 10.0.2.2 is the emulator's alias for the host machine's localhost.
    // Point this at your real (HTTPS) host for physical devices / production.
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
    private static Retrofit authorizedRetrofit = null;

    /** For the public auth endpoints (register / login). */
    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = buildRetrofit(baseClient().build());
        }
        return retrofit.create(ApiService.class);
    }

    /** For endpoints that need the signed-in user: adds "Authorization: Bearer <token>" to every request. */
    public static synchronized ShoppingApi getShoppingApi(Context context) {
        if (authorizedRetrofit == null) {
            Context appContext = context.getApplicationContext();
            AuthRepository authRepository = new AuthRepository();

            OkHttpClient client = baseClient()
                    .addInterceptor(chain -> {
                        // Read on every request, so a new login is picked up without rebuilding the client
                        String token = authRepository.getToken(appContext);
                        Request request = chain.request();
                        if (token != null) {
                            request = request.newBuilder().header("Authorization", "Bearer " + token).build();
                        }
                        return chain.proceed(request);
                    })
                    .build();
            authorizedRetrofit = buildRetrofit(client);
        }
        return authorizedRetrofit.create(ShoppingApi.class);
    }

    private static OkHttpClient.Builder baseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // BODY logging dumps full request/response bodies — including
        // passwords on register/login and the JWT on login — to logcat.
        // That must never happen in a release build.
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS);
    }

    private static Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
