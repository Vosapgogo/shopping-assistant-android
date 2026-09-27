package com.shoppingassistant.database;

/**
 * Where a locally stored row stands relative to the server. Room is the source of truth for the UI;
 * anything not SYNCED still has to be sent (Sprint 6 adds a background retry for these).
 */
public enum SyncStatus {
    /** Same as on the server. */
    SYNCED,
    /** Created on this device, the server doesn't have it yet (no server id). */
    PENDING_CREATE,
    /** Changed on this device after it was synced. */
    PENDING_UPDATE,
    /** Deleted on this device; kept (hidden) until the server confirms the delete. */
    PENDING_DELETE,
    /** The server rejected it (e.g. validation); retrying as-is won't help. */
    FAILED
}
