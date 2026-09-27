package com.shoppingassistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.repository.ShoppingListRepository;
import com.shoppingassistant.ui.ListDialogs;
import com.shoppingassistant.ui.TopBarView;
import com.shoppingassistant.util.SystemBarInsets;

/** One shopping list. For now it only has its empty state; adding items comes next. */
public class ShoppingListDetailActivity extends AppCompatActivity {

    private static final String EXTRA_LOCAL_ID = "local_id";

    private ShoppingListEntity currentList;

    public static Intent newIntent(Context context, long localId) {
        return new Intent(context, ShoppingListDetailActivity.class).putExtra(EXTRA_LOCAL_ID, localId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);
        SystemBarInsets.apply(findViewById(R.id.root), true);

        long localId = getIntent().getLongExtra(EXTRA_LOCAL_ID, -1);
        ShoppingListRepository repository = ShoppingListRepository.getInstance(this);
        TopBarView topBar = findViewById(R.id.top_bar);

        topBar.setMenu(R.menu.menu_list_actions, item -> {
            if (currentList == null) {
                return false;
            }
            if (item.getItemId() == R.id.action_rename_list) {
                ListDialogs.showRename(this, currentList, name -> repository.renameList(currentList.localId, name));
                return true;
            } else if (item.getItemId() == R.id.action_delete_list) {
                ListDialogs.showDelete(this, currentList, () -> repository.deleteList(currentList.localId));
                return true;
            }
            return false;
        });

        // Follows the list in Room, so a rename shows up at once and a delete closes the screen
        repository.observeList(localId).observe(this, list -> {
            if (list == null) {
                finish();
                return;
            }
            currentList = list;
            topBar.setTitle(list.name);
        });

        MaterialButton btnAddItem = findViewById(R.id.btn_add_item);
        btnAddItem.setOnClickListener(v ->
                Toast.makeText(this, R.string.msg_add_item_coming, Toast.LENGTH_SHORT).show());

        MaterialButton btnAddWithVoice = findViewById(R.id.btn_add_with_voice);
        btnAddWithVoice.setOnClickListener(v ->
                Toast.makeText(this, R.string.msg_voice_coming, Toast.LENGTH_SHORT).show());
    }
}
