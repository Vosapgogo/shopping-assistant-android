package com.shoppingassistant.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.shoppingassistant.model.ShoppingItem;

@Dao
public interface ShoppingItemDao {

    @Insert
    void insert(ShoppingItem item);

    @Update
    void update(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Query("SELECT * FROM shopping_items")
    List<ShoppingItem> getAllItems();
}