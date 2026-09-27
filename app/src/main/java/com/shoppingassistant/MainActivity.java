package com.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.shoppingassistant.database.ShoppingListEntity;
import com.shoppingassistant.repository.AuthRepository;
import com.shoppingassistant.repository.ShoppingListRepository;
import com.shoppingassistant.ui.ListDialogs;
import com.shoppingassistant.ui.PageHeaderView;
import com.shoppingassistant.ui.ShoppingListAdapter;
import com.shoppingassistant.ui.ShoppingListsViewModel;
import com.shoppingassistant.util.SystemBarInsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private ShoppingListsViewModel viewModel;
    private ShoppingListAdapter adapter;
    private TextView tvNoMatch;
    private List<ShoppingListEntity> allLists = Collections.emptyList();
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.apply(findViewById(R.id.root), false);

        authRepository = new AuthRepository();

        MaterialButton btnCreateList = findViewById(R.id.btn_create_list);
        btnCreateList.setOnClickListener(v -> openCreateList());
        FloatingActionButton fabCreateList = findViewById(R.id.fab_create_list);
        fabCreateList.setOnClickListener(v -> openCreateList());

        setUpLists(fabCreateList);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        View contentLists = findViewById(R.id.content_lists);
        PageHeaderView pageHeader = findViewById(R.id.page_header);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_lists) {
                pageHeader.setTitle(R.string.title_my_lists);
                contentLists.setVisibility(View.VISIBLE);

                return true;
            } else if (itemId == R.id.nav_prices) {
                pageHeader.setTitle(R.string.nav_prices);
                contentLists.setVisibility(View.GONE);

                return true;
            } else if (itemId == R.id.nav_map) {
                pageHeader.setTitle(R.string.nav_map);
                contentLists.setVisibility(View.GONE);

                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

                return false;
            }

            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // The token may have expired while the app sat in the background
        if (!authRepository.isLoggedIn(this)) {
            goToWelcome();
            return;
        }

        // Sends lists that are still only on the phone and pulls changes made on other devices
        viewModel.refresh();
    }

    private void setUpLists(FloatingActionButton fabCreateList) {
        View emptyState = findViewById(R.id.empty_state);
        View listsContainer = findViewById(R.id.lists_container);
        tvNoMatch = findViewById(R.id.tv_no_match);
        RecyclerView rvLists = findViewById(R.id.rv_lists);
        adapter = new ShoppingListAdapter(new ShoppingListAdapter.Listener() {
            @Override
            public void onOpen(ShoppingListEntity list) {
                startActivity(ShoppingListDetailActivity.newIntent(MainActivity.this, list.localId));
            }

            @Override
            public void onRename(ShoppingListEntity list) {
                ListDialogs.showRename(MainActivity.this, list, name -> viewModel.renameList(list, name));
            }

            @Override
            public void onDelete(ShoppingListEntity list) {
                ListDialogs.showDelete(MainActivity.this, list, () -> viewModel.deleteList(list));
            }
        });
        rvLists.setLayoutManager(new LinearLayoutManager(this));
        rvLists.setAdapter(adapter);
        // New and just-changed lists are sorted to the top; without this they'd land above the visible
        // area, because RecyclerView keeps the previous first card in place
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    rvLists.scrollToPosition(0);
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (toPosition == 0) {
                    rvLists.scrollToPosition(0);
                }
            }
        });

        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                showFilteredLists();
            }
        });

        viewModel = new ViewModelProvider(this).get(ShoppingListsViewModel.class);

        // The lists come from Room, so they show up at once — also without internet
        viewModel.getLists().observe(this, lists -> {
            allLists = lists != null ? lists : Collections.emptyList();
            boolean empty = allLists.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            listsContainer.setVisibility(empty ? View.GONE : View.VISIBLE);
            fabCreateList.setVisibility(empty ? View.GONE : View.VISIBLE);
            showFilteredLists();
        });

        viewModel.getSyncResult().observe(this, event -> {
            ShoppingListRepository.SyncResult result = event.getContentIfNotHandled();
            if (result == ShoppingListRepository.SyncResult.SESSION_EXPIRED) {
                // The server no longer accepts the token: sign in again. Local lists are kept,
                // and anything not synced yet goes out after the next login.
                authRepository.clearToken(this);
                Toast.makeText(this, R.string.msg_session_expired, Toast.LENGTH_LONG).show();
                goToWelcome();
            }
            // OFFLINE / SERVER_ERROR: nothing to do here — the lists are on the phone, and cards
            // that haven't reached the server say "Not synced yet"
        });
    }

    /** Shows the lists whose name contains the search text (all of them when it's empty). */
    private void showFilteredLists() {
        List<ShoppingListEntity> shown;
        if (searchQuery.isEmpty()) {
            shown = allLists;
        } else {
            String query = searchQuery.toLowerCase(Locale.getDefault());
            shown = new ArrayList<>();
            for (ShoppingListEntity list : allLists) {
                if (list.name.toLowerCase(Locale.getDefault()).contains(query)) {
                    shown.add(list);
                }
            }
        }
        adapter.submitList(shown);
        tvNoMatch.setVisibility(shown.isEmpty() && !allLists.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openCreateList() {
        startActivity(new Intent(this, CreateListActivity.class));
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
