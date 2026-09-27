package com.shoppingassistant.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.database.SyncStatus;
import com.shoppingassistant.network.ShoppingApi.ShoppingListSummaryDto;

import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class ShoppingListMergerTest {

    private static final String UPDATED_AT = "2026-09-27T13:48:28.066Z";

    @Test
    public void listOnlyOnServerIsAddedAsSynced() {
        // e.g. the app was reinstalled or this is a new phone
        ShoppingListMerger.Result result = ShoppingListMerger.merge(
                Collections.emptyList(), List.of(server(7L, "Weekly groceries", 12, 8)));

        assertEquals(1, result.toInsert.size());
        ShoppingListEntity added = result.toInsert.get(0);
        assertEquals(Long.valueOf(7L), added.serverId);
        assertEquals("Weekly groceries", added.name);
        assertEquals(12, added.itemCount);
        assertEquals(8, added.purchasedCount);
        assertEquals(Instant.parse(UPDATED_AT).toEpochMilli(), added.updatedAt);
        assertEquals(SyncStatus.SYNCED, added.syncStatus);
    }

    @Test
    public void syncedListTakesTheServersVersion() {
        ShoppingListEntity local = local(7L, "Old name", SyncStatus.SYNCED);

        ShoppingListMerger.Result result = ShoppingListMerger.merge(
                List.of(local), List.of(server(7L, "Renamed on laptop", 3, 1)));

        assertEquals(List.of(local), result.toUpdate);
        assertEquals("Renamed on laptop", local.name);
        assertEquals(3, local.itemCount);
        assertTrue(result.toInsert.isEmpty());
        assertTrue(result.toDelete.isEmpty());
    }

    @Test
    public void unsentLocalChangesAreNotOverwritten() {
        ShoppingListEntity local = local(7L, "Changed offline", SyncStatus.PENDING_UPDATE);

        ShoppingListMerger.Result result = ShoppingListMerger.merge(
                List.of(local), List.of(server(7L, "Server name", 0, 0)));

        assertTrue(result.toUpdate.isEmpty());
        assertEquals("Changed offline", local.name);
        assertEquals(SyncStatus.PENDING_UPDATE, local.syncStatus);
    }

    @Test
    public void listCreatedOfflineIsKept() {
        ShoppingListEntity local = ShoppingListEntity.newLocal("Made without internet", 1000L);

        ShoppingListMerger.Result result = ShoppingListMerger.merge(List.of(local), Collections.emptyList());

        assertTrue(result.toDelete.isEmpty());
        assertTrue(result.toInsert.isEmpty());
        assertNull(local.serverId);
    }

    @Test
    public void syncedListMissingOnServerWasDeletedElsewhere() {
        ShoppingListEntity deletedElsewhere = local(7L, "Deleted on laptop", SyncStatus.SYNCED);
        ShoppingListEntity pending = local(8L, "Changed here", SyncStatus.PENDING_UPDATE);

        ShoppingListMerger.Result result = ShoppingListMerger.merge(
                List.of(deletedElsewhere, pending), Collections.emptyList());

        assertEquals(1, result.toDelete.size());
        assertSame(deletedElsewhere, result.toDelete.get(0));
    }

    @Test
    public void unreadableTimestampKeepsTheLocalValue() {
        assertEquals(42L, ShoppingListMerger.parseMillis("not a date", 42L));
        assertEquals(42L, ShoppingListMerger.parseMillis(null, 42L));
    }

    private static ShoppingListSummaryDto server(long id, String name, int itemCount, int purchasedCount) {
        ShoppingListSummaryDto dto = new ShoppingListSummaryDto();
        dto.id = id;
        dto.name = name;
        dto.status = "ACTIVE";
        dto.itemCount = itemCount;
        dto.purchasedCount = purchasedCount;
        dto.createdAt = UPDATED_AT;
        dto.updatedAt = UPDATED_AT;
        return dto;
    }

    private static ShoppingListEntity local(long serverId, String name, SyncStatus status) {
        ShoppingListEntity list = ShoppingListEntity.newLocal(name, 1000L);
        list.serverId = serverId;
        list.syncStatus = status;
        return list;
    }
}
