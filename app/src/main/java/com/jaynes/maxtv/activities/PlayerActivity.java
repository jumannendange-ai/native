package com.jaynes.maxtv.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.*;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.*;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;
import androidx.media3.datasource.*;
import com.jaynes.maxtv.R;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private TextView tvTitle;
    private View loadingView;

    private String channelName;
    private String channelUrl;
    private String channelType;
    private String channelKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen landscape
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        // Get data
        channelName = getIntent().getStringExtra("channel_name");
        channelUrl  = getIntent().getStringExtra("channel_url");
        channelType = getIntent().getStringExtra("channel_type");
        channelKey  = getIntent().getStringExtra("channel_key");

        playerView  = findViewById(R.id.playerView);
        tvTitle     = findViewById(R.id.tvTitle);
        loadingView = findViewById(R.id.loadingView);

        tvTitle.setText(channelName);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        initPlayer();
    }

    private void initPlayer() {
        loadingView.setVisibility(View.VISIBLE);

        // Build DRM manager kama ina ClearKey
        DrmSessionManagerProvider drmProvider = null;
        if (channelKey != null && !channelKey.isEmpty() && channelKey.contains(":")) {
            String[] parts = channelKey.split(":");
            if (parts.length == 2) {
                String kid = parts[0];
                String key = parts[1];

                Map<String, String> keyMap = new HashMap<>();
                keyMap.put(kid, key);

                // ClearKey DRM
                try {
                    LocalMediaDrmCallback drmCallback = new LocalMediaDrmCallback(
                        buildClearKeyJson(kid, key).getBytes()
                    );
                    DrmSessionManager drmManager = new DefaultDrmSessionManager.Builder()
                        .setUuidAndExoMediaDrmProvider(
                            C.CLEARKEY_UUID,
                            FrameworkMediaDrm.DEFAULT_PROVIDER
                        )
                        .build(drmCallback);
                    drmProvider = mediaItem -> drmManager;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Build ExoPlayer
        ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(this);
        exoPlayer = playerBuilder.build();
        playerView.setPlayer(exoPlayer);

        // Build media source
        DefaultDataSource.Factory dataSourceFactory =
            new DefaultDataSource.Factory(this,
                new DefaultHttpDataSource.Factory()
                    .setUserAgent("JaynesMaxTV-Android/1.0")
                    .setDefaultRequestProperties(buildHeaders())
            );

        MediaItem mediaItem = new MediaItem.Builder()
            .setUri(Uri.parse(channelUrl))
            .build();

        MediaSource mediaSource;
        if ("DASH".equalsIgnoreCase(channelType)) {
            DashMediaSource.Factory factory = new DashMediaSource.Factory(dataSourceFactory);
            if (drmProvider != null) factory.setDrmSessionManagerProvider(drmProvider);
            mediaSource = factory.createMediaSource(mediaItem);
        } else {
            HlsMediaSource.Factory factory = new HlsMediaSource.Factory(dataSourceFactory);
            if (drmProvider != null) factory.setDrmSessionManagerProvider(drmProvider);
            mediaSource = factory.createMediaSource(mediaItem);
        }

        // Player listeners
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                    loadingView.setVisibility(View.GONE);
                } else if (state == Player.STATE_BUFFERING) {
                    loadingView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                loadingView.setVisibility(View.GONE);
                showError("Imeshindwa kupakia: " + error.getMessage());
            }
        });

        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private String buildClearKeyJson(String kid, String key) {
        return "{\"keys\":[{\"kty\":\"oct\",\"k\":\"" + key + "\",\"kid\":\"" + kid + "\"}],\"type\":\"temporary\"}";
    }

    private Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/112.0 Mobile Safari/537.36");
        headers.put("Referer", "https://dde.ct.ws/");
        headers.put("Origin", "https://dde.ct.ws");
        return headers;
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
