package com.jaynes.maxtv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MainActivity — Channels grid na category filter
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView      rvChannels;
    private ChannelAdapter    adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar        progress;
    private TextView           tvEmpty;
    private ChipGroup          chipGroup;

    private SessionManager     session;
    private final Handler      mainHandler = new Handler(Looper.getMainLooper());

    private final List<Channel> allChannels  = new ArrayList<>();
    private String              activeFilter = "ZOTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("JAYNES MAX TV");
        }

        // Views
        rvChannels   = findViewById(R.id.rv_channels);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progress     = findViewById(R.id.progress);
        tvEmpty      = findViewById(R.id.tv_empty);
        chipGroup    = findViewById(R.id.chip_group);

        // RecyclerView — 2 columns grid
        adapter = new ChannelAdapter(this, this::onChannelClick);
        rvChannels.setLayoutManager(new GridLayoutManager(this, 2));
        rvChannels.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(0xFFE8001D);
        swipeRefresh.setOnRefreshListener(this::loadChannels);

        loadChannels();
    }

    // ── Load channels from API ─────────────────────────
    private void loadChannels() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        String token = session.getToken();

        ApiClient.getAllChannels(token, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject response) {
                mainHandler.post(() -> {
                    swipeRefresh.setRefreshing(false);
                    progress.setVisibility(View.GONE);
                    parseChannels(response);
                });
            }
            @Override public void onError(String message, int httpCode) {
                mainHandler.post(() -> {
                    swipeRefresh.setRefreshing(false);
                    progress.setVisibility(View.GONE);
                    if (allChannels.isEmpty()) {
                        tvEmpty.setText("Hitilafu: " + message);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Kushindwa kusasisha: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void parseChannels(JSONObject response) {
        allChannels.clear();

        JSONArray arr = response.optJSONArray("channels");
        if (arr == null) arr = response.optJSONArray("data");
        if (arr == null) {
            tvEmpty.setText("Channels hazipatikani");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        Set<String> categories = new LinkedHashSet<>();
        categories.add("ZOTE");

        for (int i = 0; i < arr.length(); i++) {
            try {
                Channel ch = Channel.fromJson(arr.getJSONObject(i));
                if (ch.isValid()) {
                    allChannels.add(ch);
                    categories.add(ch.category);
                }
            } catch (Exception e) { /* skip bad entry */ }
        }

        buildCategoryChips(new ArrayList<>(categories));
        applyFilter(activeFilter);
    }

    // ── Category chips ─────────────────────────────────
    private void buildCategoryChips(List<String> categories) {
        chipGroup.removeAllViews();
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(activeFilter));
            chip.setChipBackgroundColorResource(R.color.chip_bg);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text));
            chip.setOnClickListener(v -> applyFilter(cat));
            chipGroup.addView(chip);
        }
    }

    private void applyFilter(String category) {
        activeFilter = category;
        adapter.filterByCategory(category, allChannels);

        if (adapter.getItemCount() == 0) {
            tvEmpty.setText("Channels za '" + category + "' hazipatikani");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // ── Channel click → PlayerActivity ────────────────
    private void onChannelClick(Channel ch) {
        // Angalia subscription kwa premium channels
        if (!ch.isFree && !session.isSubscribed()) {
            Toast.makeText(this,
                    "Channel hii inahitaji subscription. Nenda Akaunti → Lipa.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("channel_name",  ch.name);
        intent.putExtra("channel_url",   ch.url);
        intent.putExtra("channel_type",  ch.type);
        intent.putExtra("channel_key",   ch.key);
        intent.putExtra("channel_image", ch.image);
        intent.putExtra("has_drm",       ch.hasDrm);
        startActivity(intent);
    }

    // ── Options menu ───────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            doLogout();
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            loadChannels();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLogout() {
        String token = session.getToken();
        ApiClient.logout(token, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject r) {}
            @Override public void onError(String m, int c) {}
        });
        session.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
