package com.shoppingassistant.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Backend /api/v1/shopping-lists. Every call needs the JWT, which ApiClient adds automatically. */
public interface ShoppingApi {

    @GET("api/v1/shopping-lists")
    Call<List<ShoppingListSummaryDto>> getLists();

    @POST("api/v1/shopping-lists")
    Call<ShoppingListDto> createList(@Body CreateListRequest request);

    @PUT("api/v1/shopping-lists/{id}")
    Call<ShoppingListDto> updateList(@Path("id") long id, @Body UpdateListRequest request);

    @DELETE("api/v1/shopping-lists/{id}")
    Call<Void> deleteList(@Path("id") long id);

    class CreateListRequest {
        public String name;

        public CreateListRequest(String name) {
            this.name = name;
        }
    }

    class UpdateListRequest {
        public String name;
        public String status;

        public UpdateListRequest(String name, String status) {
            this.name = name;
            this.status = status;
        }
    }

    /** One row of GET /shopping-lists. Timestamps are ISO-8601 strings, e.g. "2026-09-27T13:48:28.066Z". */
    class ShoppingListSummaryDto {
        public Long id;
        public String name;
        public String status;
        public int itemCount;
        public int purchasedCount;
        public String createdAt;
        public String updatedAt;
    }

    /** A full list as returned by POST (its items aren't needed yet, so they're not mapped). */
    class ShoppingListDto {
        public Long id;
        public String name;
        public String status;
        public String createdAt;
        public String updatedAt;
    }
}
