package com.shoppingassistant.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.repository.ShoppingListRepository;
import com.shoppingassistant.util.Event;

import java.util.List;

/** Data for My Lists; survives rotation, and reads everything through the repository. */
public class ShoppingListsViewModel extends AndroidViewModel {

    private final ShoppingListRepository repository;
    private final LiveData<List<ShoppingListEntity>> lists;

    public ShoppingListsViewModel(@NonNull Application application) {
        super(application);
        repository = ShoppingListRepository.getInstance(application);
        lists = repository.observeLists();
    }

    public LiveData<List<ShoppingListEntity>> getLists() {
        return lists;
    }

    public LiveData<Event<ShoppingListRepository.SyncResult>> getSyncResult() {
        return repository.getSyncResult();
    }

    public void refresh() {
        repository.refresh();
    }

    public void renameList(ShoppingListEntity list, String name) {
        repository.renameList(list.localId, name);
    }

    public void deleteList(ShoppingListEntity list) {
        repository.deleteList(list.localId);
    }
}
