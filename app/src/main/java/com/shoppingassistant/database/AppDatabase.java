package com.shoppingassistant.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ShoppingListEntity.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "shopping_assistant.db";

    private static volatile AppDatabase instance;

    public abstract ShoppingListDao shoppingListDao();

    /** One database for the whole app; opening it is expensive. */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return instance;
    }
}
