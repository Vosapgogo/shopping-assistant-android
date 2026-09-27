package com.shoppingassistant.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppingassistant.database.AppDatabase;
import com.shoppingassistant.database.ShoppingListDao;
import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.database.SyncStatus;
import com.shoppingassistant.network.ApiClient;
import com.shoppingassistant.network.ShoppingApi;
import com.shoppingassistant.util.Event;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Shopping lists, local-first: screens read and write Room only, so everything works instantly and
 * offline; the server is the copy that survives a lost phone or a reinstall.
 *
 * <ul>
 *     <li>{@link #createList}, {@link #renameList} and {@link #deleteList} change Room right away and
 *     mark the row as pending, then send the change to the server.</li>
 *     <li>{@link #refresh} sends anything still pending, then pulls the server's lists into Room.</li>
 * </ul>
 * If the server can't be reached, pending lists stay in Room and go out on the next refresh
 * (Sprint 6 adds a background retry with WorkManager).
 */
public class ShoppingListRepository {

    /** How the last attempt to talk to the server went. */
    public enum SyncResult {
        OK,
        OFFLINE,
        SESSION_EXPIRED,
        SERVER_ERROR
    }

    private static final String PREFS_FILE = "shopping_lists";
    private static final String KEY_OWNER_EMAIL = "owner_email";

    private static volatile ShoppingListRepository instance;

    private final Context appContext;
    private final AppDatabase database;
    private final ShoppingListDao dao;
    private final ShoppingApi api;
    // One thread: syncs never overlap, so a list can't be sent to the server twice at the same time
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Event<SyncResult>> syncResult = new MutableLiveData<>();

    private ShoppingListRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(appContext);
        this.dao = database.shoppingListDao();
        this.api = ApiClient.getShoppingApi(appContext);
    }

    public static ShoppingListRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ShoppingListRepository.class) {
                if (instance == null) {
                    instance = new ShoppingListRepository(context);
                }
            }
        }
        return instance;
    }

    public LiveData<List<ShoppingListEntity>> observeLists() {
        return dao.observeLists();
    }

    public LiveData<ShoppingListEntity> observeList(long localId) {
        return dao.observeList(localId);
    }

    public LiveData<Event<SyncResult>> getSyncResult() {
        return syncResult;
    }

    public void createList(String name) {
        executor.execute(() -> {
            dao.insert(ShoppingListEntity.newLocal(name, System.currentTimeMillis()));
            syncResult.postValue(new Event<>(pushPendingChanges()));
        });
    }

    public void renameList(long localId, String name) {
        executor.execute(() -> {
            ShoppingListEntity list = dao.getById(localId);
            if (list == null) {
                return;
            }
            list.name = name;
            list.updatedAt = System.currentTimeMillis();
            // A list the server doesn't have yet is simply created with the new name
            if (list.syncStatus != SyncStatus.PENDING_CREATE) {
                list.syncStatus = SyncStatus.PENDING_UPDATE;
            }
            dao.update(list);
            syncResult.postValue(new Event<>(pushPendingChanges()));
        });
    }

    public void deleteList(long localId) {
        executor.execute(() -> {
            ShoppingListEntity list = dao.getById(localId);
            if (list == null) {
                return;
            }
            if (list.serverId == null) {
                // Never reached the server: nothing to tell it
                dao.delete(list);
            } else {
                // Hidden right away, removed from Room once the server confirms
                list.syncStatus = SyncStatus.PENDING_DELETE;
                dao.update(list);
            }
            syncResult.postValue(new Event<>(pushPendingChanges()));
        });
    }

    public void refresh() {
        executor.execute(() -> {
            SyncResult pushed = pushPendingChanges();
            syncResult.postValue(new Event<>(pushed == SyncResult.OK ? pullFromServer() : pushed));
        });
    }

    /**
     * Call after a successful login. Lists on this device belong to one account: if someone else
     * signs in, the previous user's lists are removed so they don't show up in another account.
     */
    public void onSignedIn(String email) {
        executor.execute(() -> {
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
            String owner = prefs.getString(KEY_OWNER_EMAIL, null);
            if (owner != null && !owner.equalsIgnoreCase(email)) {
                dao.clear();
            }
            prefs.edit().putString(KEY_OWNER_EMAIL, email).apply();
        });
    }

    /** Call on logout: removes this account's lists from the device (they stay on the server). */
    public void clearLocalData() {
        executor.execute(() -> {
            dao.clear();
            appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE).edit().clear().apply();
        });
    }

    /**
     * Sends every change made on this device that the server doesn't have yet. Stops at the first
     * connection or session problem; whatever is left stays pending for next time. Runs on the executor.
     */
    private SyncResult pushPendingChanges() {
        try {
            for (ShoppingListEntity list : dao.getBySyncStatus(SyncStatus.PENDING_CREATE)) {
                Response<ShoppingApi.ShoppingListDto> response =
                        api.createList(new ShoppingApi.CreateListRequest(list.name)).execute();
                SyncResult problem = handleResponse(list, response);
                if (problem != null) {
                    return problem;
                }
            }

            for (ShoppingListEntity list : dao.getBySyncStatus(SyncStatus.PENDING_UPDATE)) {
                Response<ShoppingApi.ShoppingListDto> response = api.updateList(list.serverId,
                        new ShoppingApi.UpdateListRequest(list.name, list.status)).execute();
                if (response.code() == 404) {
                    // Deleted on another device meanwhile
                    dao.delete(list);
                    continue;
                }
                SyncResult problem = handleResponse(list, response);
                if (problem != null) {
                    return problem;
                }
            }

            for (ShoppingListEntity list : dao.getBySyncStatus(SyncStatus.PENDING_DELETE)) {
                Response<Void> response = api.deleteList(list.serverId).execute();
                if (response.isSuccessful() || response.code() == 404) {
                    // 404: already gone on the server, which is what we wanted
                    dao.delete(list);
                } else if (response.code() == 401) {
                    return SyncResult.SESSION_EXPIRED;
                } else {
                    return SyncResult.SERVER_ERROR;
                }
            }
        } catch (IOException e) {
            // No connection: the changes stay pending and are sent next time
            return SyncResult.OFFLINE;
        }
        return SyncResult.OK;
    }

    /**
     * Applies the server's answer to a create/update: on success the row takes the server's id and
     * timestamps. Returns the reason to stop syncing, or null to carry on.
     */
    private SyncResult handleResponse(ShoppingListEntity list, Response<ShoppingApi.ShoppingListDto> response) {
        if (response.isSuccessful() && response.body() != null) {
            ShoppingApi.ShoppingListDto saved = response.body();
            list.serverId = saved.id;
            list.createdAt = ShoppingListMerger.parseMillis(saved.createdAt, list.createdAt);
            list.updatedAt = ShoppingListMerger.parseMillis(saved.updatedAt, list.updatedAt);
            list.syncStatus = SyncStatus.SYNCED;
            dao.update(list);
            return null;
        }
        if (response.code() == 401) {
            return SyncResult.SESSION_EXPIRED;
        }
        if (response.code() >= 400 && response.code() < 500) {
            // The server won't accept this list as it is; don't resend it forever
            list.syncStatus = SyncStatus.FAILED;
            dao.update(list);
            return null;
        }
        return SyncResult.SERVER_ERROR;
    }

    /** Replaces synced lists with the server's version. Runs on the executor. */
    private SyncResult pullFromServer() {
        try {
            Response<List<ShoppingApi.ShoppingListSummaryDto>> response = api.getLists().execute();
            if (response.code() == 401) {
                return SyncResult.SESSION_EXPIRED;
            }
            if (!response.isSuccessful() || response.body() == null) {
                return SyncResult.SERVER_ERROR;
            }

            List<ShoppingApi.ShoppingListSummaryDto> serverLists = response.body();
            database.runInTransaction(() -> {
                ShoppingListMerger.Result merge = ShoppingListMerger.merge(dao.getAll(), serverLists);
                dao.insertAll(merge.toInsert);
                dao.updateAll(merge.toUpdate);
                dao.deleteAll(merge.toDelete);
            });
            return SyncResult.OK;
        } catch (IOException e) {
            return SyncResult.OFFLINE;
        }
    }
}
