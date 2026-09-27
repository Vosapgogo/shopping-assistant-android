package com.shoppingassistant.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShoppingListDao {

    /** What "My Lists" shows: everything except lists deleted on this device, newest change first. */
    @Query("SELECT * FROM shopping_lists WHERE sync_status != 'PENDING_DELETE' ORDER BY updated_at DESC")
    LiveData<List<ShoppingListEntity>> observeLists();

    /** One list for its details screen; emits null once it's deleted. */
    @Query("SELECT * FROM shopping_lists WHERE local_id = :localId AND sync_status != 'PENDING_DELETE'")
    LiveData<ShoppingListEntity> observeList(long localId);

    @Query("SELECT * FROM shopping_lists")
    List<ShoppingListEntity> getAll();

    @Query("SELECT * FROM shopping_lists WHERE local_id = :localId")
    ShoppingListEntity getById(long localId);

    @Query("SELECT * FROM shopping_lists WHERE sync_status = :status")
    List<ShoppingListEntity> getBySyncStatus(SyncStatus status);

    @Insert
    long insert(ShoppingListEntity list);

    @Insert
    void insertAll(List<ShoppingListEntity> lists);

    @Update
    void update(ShoppingListEntity list);

    @Update
    void updateAll(List<ShoppingListEntity> lists);

    @Delete
    void delete(ShoppingListEntity list);

    @Delete
    void deleteAll(List<ShoppingListEntity> lists);

    @Query("DELETE FROM shopping_lists")
    void clear();
}
