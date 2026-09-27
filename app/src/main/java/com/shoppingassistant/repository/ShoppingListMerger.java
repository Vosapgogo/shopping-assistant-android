package com.shoppingassistant.repository;

import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.database.SyncStatus;
import com.shoppingassistant.network.ShoppingApi.ShoppingListSummaryDto;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Decides how the server's lists are applied to the local ones. Kept free of Room and Retrofit so the
 * rules can be unit tested:
 * <ul>
 *     <li>a list only the server has (e.g. after reinstalling or on a new phone) is added;</li>
 *     <li>a synced list is overwritten with the server's version;</li>
 *     <li>a list with local changes not sent yet is never overwritten — the server gets it later;</li>
 *     <li>a synced list the server no longer has was deleted elsewhere and is removed.</li>
 * </ul>
 */
public final class ShoppingListMerger {

    public static final class Result {
        public final List<ShoppingListEntity> toInsert = new ArrayList<>();
        public final List<ShoppingListEntity> toUpdate = new ArrayList<>();
        public final List<ShoppingListEntity> toDelete = new ArrayList<>();
    }

    private ShoppingListMerger() {
    }

    public static Result merge(List<ShoppingListEntity> local, List<ShoppingListSummaryDto> server) {
        Result result = new Result();

        Map<Long, ShoppingListEntity> localByServerId = new HashMap<>();
        for (ShoppingListEntity list : local) {
            if (list.serverId != null) {
                localByServerId.put(list.serverId, list);
            }
        }

        Set<Long> serverIds = new HashSet<>();
        for (ShoppingListSummaryDto dto : server) {
            serverIds.add(dto.id);
            ShoppingListEntity existing = localByServerId.get(dto.id);
            if (existing == null) {
                ShoppingListEntity created = new ShoppingListEntity();
                applyServer(created, dto);
                result.toInsert.add(created);
            } else if (existing.syncStatus == SyncStatus.SYNCED) {
                applyServer(existing, dto);
                result.toUpdate.add(existing);
            }
            // Otherwise the local copy has unsent changes and wins for now
        }

        for (ShoppingListEntity list : local) {
            if (list.serverId != null && !serverIds.contains(list.serverId)
                    && list.syncStatus == SyncStatus.SYNCED) {
                result.toDelete.add(list);
            }
        }
        return result;
    }

    /** Copies the server's fields into a local row and marks it synced. */
    static void applyServer(ShoppingListEntity list, ShoppingListSummaryDto dto) {
        list.serverId = dto.id;
        list.name = dto.name != null ? dto.name : "";
        list.status = dto.status != null ? dto.status : "ACTIVE";
        list.itemCount = dto.itemCount;
        list.purchasedCount = dto.purchasedCount;
        list.createdAt = parseMillis(dto.createdAt, list.createdAt);
        list.updatedAt = parseMillis(dto.updatedAt, list.updatedAt);
        list.syncStatus = SyncStatus.SYNCED;
    }

    static long parseMillis(String isoTimestamp, long fallback) {
        if (isoTimestamp == null) {
            return fallback;
        }
        try {
            return Instant.parse(isoTimestamp).toEpochMilli();
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }
}
