package com.shoppingassistant;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateListActivity extends AppCompatActivity {

    private TextInputLayout tilListName;
    private TextInputEditText etListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        tilListName = findViewById(R.id.til_list_name);
        etListName = findViewById(R.id.et_list_name);

        // Back arrow and Cancel both just close the screen and return to MainActivity
        ImageView btnBack = findViewById(R.id.btn_back_create_list);
        btnBack.setOnClickListener(v -> finish());

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

        // TODO: save the list via the backend once the lists API is wired up
        String msg = getString(R.string.msg_list_created) + listName;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}
