package com.shoppingassistant.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.shoppingassistant.R;
import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.database.SyncStatus;

/** The cards on My Lists. */
public class ShoppingListAdapter extends ListAdapter<ShoppingListEntity, ShoppingListAdapter.ViewHolder> {

    /** Taps on a card and on its "⋮" menu. */
    public interface Listener {
        void onOpen(ShoppingListEntity list);

        void onRename(ShoppingListEntity list);

        void onDelete(ShoppingListEntity list);
    }

    /** Background + icon colour pairs; each list gets one based on its name, so the cards differ. */
    @ColorRes
    private static final int[][] ICON_COLORS = {
            {R.color.list_icon_green_bg, R.color.list_icon_green},
            {R.color.list_icon_purple_bg, R.color.list_icon_purple},
            {R.color.list_icon_orange_bg, R.color.list_icon_orange},
            {R.color.list_icon_red_bg, R.color.list_icon_red},
            {R.color.list_icon_blue_bg, R.color.list_icon_blue},
    };

    private final Listener listener;

    public ShoppingListAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView name;
        private final TextView meta;
        private final TextView progressText;
        private final LinearProgressIndicator progress;
        private final ImageButton menuButton;
        private final Listener listener;

        ViewHolder(@NonNull View itemView, Listener listener) {
            super(itemView);
            this.listener = listener;
            icon = itemView.findViewById(R.id.iv_list_icon);
            name = itemView.findViewById(R.id.tv_list_name);
            meta = itemView.findViewById(R.id.tv_list_meta);
            progressText = itemView.findViewById(R.id.tv_list_progress);
            progress = itemView.findViewById(R.id.progress_purchased);
            menuButton = itemView.findViewById(R.id.btn_list_menu);
        }

        void bind(ShoppingListEntity list) {
            Context context = itemView.getContext();
            name.setText(list.name);

            int[] colors = ICON_COLORS[Math.floorMod(list.name.hashCode(), ICON_COLORS.length)];
            icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colors[0])));
            icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colors[1])));

            itemView.setOnClickListener(v -> listener.onOpen(list));
            menuButton.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(context, v);
                menu.inflate(R.menu.menu_list_actions);
                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_rename_list) {
                        listener.onRename(list);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_list) {
                        listener.onDelete(list);
                        return true;
                    }
                    return false;
                });
                menu.show();
            });

            String items = context.getResources()
                    .getQuantityString(R.plurals.list_item_count, list.itemCount, list.itemCount);
            if (list.syncStatus == SyncStatus.PENDING_CREATE || list.syncStatus == SyncStatus.PENDING_UPDATE) {
                // Tells the user the list is safe on the phone but not on the server yet
                meta.setText(context.getString(R.string.list_meta_not_synced, items));
            } else if (list.syncStatus == SyncStatus.FAILED) {
                meta.setText(context.getString(R.string.list_meta_sync_failed, items));
            } else {
                long now = System.currentTimeMillis();
                CharSequence updated = now - list.updatedAt < DateUtils.MINUTE_IN_MILLIS
                        ? context.getString(R.string.list_updated_just_now)
                        : DateUtils.getRelativeTimeSpanString(list.updatedAt, now, DateUtils.MINUTE_IN_MILLIS);
                meta.setText(context.getString(R.string.list_meta_updated, items, updated));
            }

            progress.setMax(Math.max(list.itemCount, 1));
            progress.setProgress(list.purchasedCount);
            progressText.setText(context.getString(R.string.list_progress, list.purchasedCount, list.itemCount));
        }
    }

    private static final DiffUtil.ItemCallback<ShoppingListEntity> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ShoppingListEntity oldItem, @NonNull ShoppingListEntity newItem) {
            return oldItem.localId == newItem.localId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ShoppingListEntity oldItem, @NonNull ShoppingListEntity newItem) {
            return oldItem.name.equals(newItem.name)
                    && oldItem.status.equals(newItem.status)
                    && oldItem.itemCount == newItem.itemCount
                    && oldItem.purchasedCount == newItem.purchasedCount
                    && oldItem.updatedAt == newItem.updatedAt
                    && oldItem.syncStatus.equals(newItem.syncStatus);
        }
    };
}
