package com.jaynes.maxtv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.jaynes.maxtv.R;
import com.jaynes.maxtv.models.Channel;
import com.jaynes.maxtv.ui.ChannelAdapter;
import com.jaynes.maxtv.utils.ApiClient;
import com.jaynes.maxtv.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ChannelAdapter.OnChannelClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ChannelAdapter adapter;
    private List<Channel> channelList = new ArrayList<>();
    private TextView tvWelcome, tvPlan, tvError;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = SessionManager.getInstance(this);

        // Auth check
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupUI();
        loadChannels();
    }

    private void initViews() {
        recyclerView  = findViewById(R.id.recyclerView);
        swipeRefresh  = findViewById(R.id.swipeRefresh);
        tvWelcome     = findViewById(R.id.tvWelcome);
        tvPlan        = findViewById(R.id.tvPlan);
        tvError       = findViewById(R.id.tvError);
        progressBar   = findViewById(R.id.progressBar);

        // Setup RecyclerView
        adapter = new ChannelAdapter(channelList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefresh
        swipeRefresh.setColorSchemeColors(0xFFE8001D, 0xFFFF6B00);
        swipeRefresh.setOnRefreshListener(this::loadChannels);
    }

    private void setupUI() {
        // Welcome message
        tvWelcome.setText("Karibu, " + session.getFirstName() + "!");

        // Plan badge
        if (session.isPremium()) {
            tvPlan.setText("PREMIUM ✓");
            tvPlan.setBackgroundColor(0xFFE8001D);
        } else if (session.isTrialActive()) {
            tvPlan.setText("MAJARIBIO");
            tvPlan.setBackgroundColor(0xFFFF6B00);
        } else {
            tvPlan.setText("LIPIA SASA");
            tvPlan.setBackgroundColor(0xFF444455);
            tvPlan.setOnClickListener(v -> openPayment());
        }

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void loadChannels() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        ApiClient.getChannels(new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        JSONArray channels = response.optJSONArray("channels");
                        if (channels != null) {
                            channelList.clear();
                            for (int i = 0; i < channels.length(); i++) {
                                JSONObject ch = channels.getJSONObject(i);
                                Channel c = new Channel();
                                c.name     = ch.optString("name");
                                c.category = ch.optString("category");
                                c.url      = ch.optString("url");
                                c.image    = ch.optString("image");
                                c.key      = ch.optString("key", null);
                                c.type     = ch.optString("type", "HLS");
                                c.source   = ch.optString("source");
                                c.isFree   = ch.optBoolean("is_free", false);
                                c.hasDrm   = ch.optBoolean("has_drm", false);
                                channelList.add(c);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        showError("Tatizo la kuchambua data.");
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Imeshindwa kupata channels: " + message);
                });
            }
        });
    }

    @Override
    public void onChannelClick(Channel channel) {
        // Check access
        if (!channel.isFree && !session.hasAccess()) {
            showPaymentDialog();
            return;
        }

        // Open player
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("channel_name",  channel.name);
        intent.putExtra("channel_url",   channel.url);
        intent.putExtra("channel_image", channel.image);
        intent.putExtra("channel_type",  channel.type);
        intent.putExtra("channel_key",   channel.key);
        startActivity(intent);
    }

    private void showPaymentDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Subscription Inahitajika")
            .setMessage("Channel hii inahitaji subscription ya JAYNES MAX TV.\n\nBei:\n• Wiki 1 — TZS 1,000\n• Mwezi 1 — TZS 3,000\n• Miezi 3 — TZS 8,000\n• Mwaka 1 — TZS 25,000\n\nWasiliana: 0616393956")
            .setPositiveButton("LIPIA SASA", (d, w) -> openPayment())
            .setNegativeButton("Baadaye", null)
            .show();
    }

    private void openPayment() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://dde.ct.ws/malipo.php"));
        startActivity(intent);
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Toka")
            .setMessage("Una uhakika unataka kutoka?")
            .setPositiveButton("Ndio", (d, w) -> {
                session.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            })
            .setNegativeButton("Hapana", null)
            .show();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading && channelList.isEmpty() ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(loading && !channelList.isEmpty());
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
