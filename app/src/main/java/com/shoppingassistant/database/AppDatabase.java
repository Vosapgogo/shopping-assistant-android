package com.shoppingassistant.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.shoppingassistant.model.ShoppingItem;

@Database(entities = {ShoppingItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ShoppingItemDao shoppingItemDao();

}
