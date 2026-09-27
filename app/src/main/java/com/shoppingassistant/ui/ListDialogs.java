package com.shoppingassistant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shoppingassistant.R;
import com.shoppingassistant.database.ShoppingListEntity;

import java.util.function.Consumer;

/** "Rename list" and "Delete list?" dialogs, shared by My Lists and the list details screen. */
public final class ListDialogs {

    private ListDialogs() {
    }

    /** Calls {@code onRename} with the new, non-empty name (not called if it didn't change). */
    public static void showRename(Context context, ShoppingListEntity list, Consumer<String> onRename) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rename_list, null);
        TextInputLayout tilRename = view.findViewById(R.id.til_rename);
        TextInputEditText etRename = view.findViewById(R.id.et_rename);
        etRename.setText(list.name);
        etRename.setSelection(list.name.length());

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_rename_list_title)
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .show();

        // Set here rather than in the builder so an empty name keeps the dialog open with an error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etRename.getText() == null ? "" : etRename.getText().toString().trim();
            if (name.isEmpty()) {
                tilRename.setError(context.getString(R.string.error_list_name_empty));
                return;
            }
            if (!name.equals(list.name)) {
                onRename.accept(name);
            }
            dialog.dismiss();
        });
    }

    public static void showDelete(Context context, ShoppingListEntity list, Runnable onDelete) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.dialog_delete_list_title, list.name))
                .setMessage(R.string.dialog_delete_list_message)
                .setPositiveButton(R.string.action_delete, (d, which) -> onDelete.run())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
