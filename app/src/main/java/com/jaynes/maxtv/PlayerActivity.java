package com.jaynes.maxtv;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.LocalMediaDrmCallback;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

/**
 * PlayerActivity — ExoPlayer na ClearKey DRM support
 *
 * Intent extras:
 *   channel_name  (String)
 *   channel_url   (String)  — HLS .m3u8 au DASH .mpd
 *   channel_type  (String)  — "HLS" | "DASH"
 *   channel_key   (String)  — "kid:key" hex strings (au tupu)
 *   has_drm       (boolean)
 */
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";

    // ClearKey UUID
    private static final UUID CLEARKEY_UUID =
            new UUID(0xE2719D58A985B3C3L, 0x8233B8CF4AE7C52AL);

    private ExoPlayer      player;
    private StyledPlayerView playerView;
    private TextView       tvTitle;
    private ImageButton    btnBack;

    private String channelName;
    private String channelUrl;
    private String channelType;
    private String channelKey;
    private boolean hasDrm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen + screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_player);

        // Extras
        channelName = getIntent().getStringExtra("channel_name");
        channelUrl  = getIntent().getStringExtra("channel_url");
        channelType = getIntent().getStringExtra("channel_type");
        channelKey  = getIntent().getStringExtra("channel_key");
        hasDrm      = getIntent().getBooleanExtra("has_drm", false);

        playerView = findViewById(R.id.player_view);
        tvTitle    = findViewById(R.id.tv_title);
        btnBack    = findViewById(R.id.btn_back);

        if (tvTitle != null) tvTitle.setText(channelName);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        initPlayer();
    }

    // ── Player init ────────────────────────────────────
    private void initPlayer() {
        if (channelUrl == null || channelUrl.isEmpty()) {
            Toast.makeText(this, "URL ya channel haipo", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("JAYNES-MAX-TV/1.0 (Android)")
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(20_000)
                .setAllowCrossProtocolRedirects(true);

        try {
            if (hasDrm && channelKey != null && !channelKey.isEmpty()) {
                buildDrmPlayer(httpFactory);
            } else {
                buildPlainPlayer(httpFactory);
            }
        } catch (Exception e) {
            Log.e(TAG, "Player init failed: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu ya player: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // ── Plain HLS/DASH (hakuna DRM) ────────────────────
    private void buildPlainPlayer(DefaultHttpDataSource.Factory httpFactory) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem item;
        if ("DASH".equalsIgnoreCase(channelType)) {
            item = new MediaItem.Builder()
                    .setUri(Uri.parse(channelUrl))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build();
        } else {
            item = new MediaItem.Builder()
                    .setUri(Uri.parse(channelUrl))
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build();
        }

        MediaSource source;
        if ("DASH".equalsIgnoreCase(channelType)) {
            source = new DashMediaSource.Factory(httpFactory).createMediaSource(item);
        } else {
            source = new HlsMediaSource.Factory(httpFactory).createMediaSource(item);
        }

        attachListeners();
        player.setMediaSource(source);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    // ── ClearKey DRM DASH ─────────────────────────────
    private void buildDrmPlayer(DefaultHttpDataSource.Factory httpFactory) throws Exception {
        // Parse "kid:key" → ClearKey JSON license
        String[] parts = channelKey.split(":");
        if (parts.length != 2) {
            Log.w(TAG, "Key format si sahihi, trying plain player");
            buildPlainPlayer(httpFactory);
            return;
        }

        String kid = parts[0].trim();
        String key = parts[1].trim();

        // ClearKey license JSON
        JSONObject licenseJson = new JSONObject();
        JSONArray keys = new JSONArray();
        JSONObject keyObj = new JSONObject();
        keyObj.put("kty", "oct");
        keyObj.put("k",   hexToBase64Url(key));
        keyObj.put("kid", hexToBase64Url(kid));
        keys.put(keyObj);
        licenseJson.put("keys", keys);
        licenseJson.put("type", "temporary");

        byte[] licenseBytes = licenseJson.toString().getBytes("UTF-8");

        DefaultDrmSessionManager drmManager = new DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(new LocalMediaDrmCallback(licenseBytes));

        MediaItem item = new MediaItem.Builder()
                .setUri(Uri.parse(channelUrl))
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build();

        DashMediaSource source = new DashMediaSource.Factory(httpFactory)
                .setDrmSessionManagerProvider(mediaItem -> drmManager)
                .createMediaSource(item);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        attachListeners();
        player.setMediaSource(source);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    // ── Hex → Base64URL (ClearKey format) ─────────────
    private String hexToBase64Url(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    // ── Player event listeners ─────────────────────────
    private void attachListeners() {
        player.addListener(new Player.Listener() {
            @Override public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                runOnUiThread(() ->
                    Toast.makeText(PlayerActivity.this,
                            "Hitilafu ya stream: " + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                );
            }
            @Override public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    Log.d(TAG, "Buffering…");
                } else if (state == Player.STATE_READY) {
                    Log.d(TAG, "Playing: " + channelName);
                }
            }
        });
    }

    // ── Lifecycle ─────────────────────────────────────
    @Override protected void onPause()   { super.onPause();   if (player != null) player.pause(); }
    @Override protected void onResume()  { super.onResume();  if (player != null) player.play(); }
    @Override protected void onDestroy() { super.onDestroy(); releasePlayer(); }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
