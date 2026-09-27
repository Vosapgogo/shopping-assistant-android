package com.shoppingassistant;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shoppingassistant.repository.ShoppingListRepository;
import com.shoppingassistant.util.SystemBarInsets;

public class CreateListActivity extends AppCompatActivity {

    private TextInputLayout tilListName;
    private TextInputEditText etListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);
        SystemBarInsets.apply(findViewById(R.id.root), true);

        tilListName = findViewById(R.id.til_list_name);
        etListName = findViewById(R.id.et_list_name);

        // The top bar's back arrow closes the screen by itself; Cancel does the same
        MaterialButton btnCancel = findViewById(R.id.btn_cancel_create_list);
        btnCancel.setOnClickListener(v -> finish());

        MaterialButton btnCreate = findViewById(R.id.btn_create_list_confirm);
        btnCreate.setOnClickListener(v -> createList());

        etListName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createList();
                return true;
            }
            return false;
        });
    }

    private void createList() {
        String listName = etListName.getText() == null ? "" : etListName.getText().toString().trim();

        if (listName.isEmpty()) {
            tilListName.setError(getString(R.string.error_list_name_empty));
            return;
        }
        tilListName.setError(null);

        // Saved on the phone right away; the repository sends it to the server in the background
        ShoppingListRepository.getInstance(this).createList(listName);

        String msg = getString(R.string.msg_list_created) + listName;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}
