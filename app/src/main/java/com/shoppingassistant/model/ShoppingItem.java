package com.shoppingassistant.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items")
public class ShoppingItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int quantity;
    public String unit;
    public boolean purchased;

    public ShoppingItem(String name, int quantity, String unit, boolean purchased) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.purchased = purchased;
    }
}