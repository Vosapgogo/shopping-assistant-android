package com.shoppingassistant.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A shopping list as stored on the device. */
@Entity(tableName = "shopping_lists", indices = {@Index(value = "server_id", unique = true)})
public class ShoppingListEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    /** The backend's id; null until the list has been created on the server. */
    @Nullable
    @ColumnInfo(name = "server_id")
    public Long serverId;

    @NonNull
    public String name = "";

    /** ACTIVE or COMPLETED, as the backend names them. */
    @NonNull
    public String status = "ACTIVE";

    @ColumnInfo(name = "item_count")
    public int itemCount;

    @ColumnInfo(name = "purchased_count")
    public int purchasedCount;

    /** Epoch millis. */
    @ColumnInfo(name = "created_at")
    public long createdAt;

    /** Epoch millis; bumped on every change so conflicts can be resolved by "last write wins". */
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @NonNull
    @ColumnInfo(name = "sync_status")
    public SyncStatus syncStatus = SyncStatus.PENDING_CREATE;

    /** A list the user just created on this device. */
    public static ShoppingListEntity newLocal(@NonNull String name, long now) {
        ShoppingListEntity list = new ShoppingListEntity();
        list.name = name;
        list.createdAt = now;
        list.updatedAt = now;
        list.syncStatus = SyncStatus.PENDING_CREATE;
        return list;
    }
}
