package com.jaynes.maxtv.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "jaynes_session";
    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    // Keys
    private static final String KEY_TOKEN        = "token";
    private static final String KEY_REFRESH      = "refresh_token";
    private static final String KEY_EMAIL        = "email";
    private static final String KEY_NAME         = "name";
    private static final String KEY_UID          = "uid";
    private static final String KEY_PLAN         = "plan";
    private static final String KEY_TRIAL_END    = "trial_end";
    private static final String KEY_SUB_END      = "sub_end";
    private static final String KEY_CREATED_AT   = "created_at";
    private static final String KEY_PROVIDER     = "provider";
    private static final String KEY_LOGGED_IN    = "logged_in";

    private SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx.getApplicationContext());
        return instance;
    }

    // ── Save full session ──────────────────────────────
    public void saveSession(String token, String refresh, String email,
                            String name, String uid, String plan,
                            String trialEnd, String subEnd, String createdAt) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_TOKEN,      token);
        editor.putString(KEY_REFRESH,    refresh);
        editor.putString(KEY_EMAIL,      email);
        editor.putString(KEY_NAME,       name);
        editor.putString(KEY_UID,        uid);
        editor.putString(KEY_PLAN,       plan);
        editor.putString(KEY_TRIAL_END,  trialEnd);
        editor.putString(KEY_SUB_END,    subEnd);
        editor.putString(KEY_CREATED_AT, createdAt);
        editor.apply();
    }

    public void logout() {
        editor.clear().apply();
    }

    // ── Getters ──────────────────────────────────────
    public boolean isLoggedIn()   { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public String  getToken()     { return prefs.getString(KEY_TOKEN,    ""); }
    public String  getRefresh()   { return prefs.getString(KEY_REFRESH,  ""); }
    public String  getEmail()     { return prefs.getString(KEY_EMAIL,    ""); }
    public String  getName()      { return prefs.getString(KEY_NAME,     "Mgeni"); }
    public String  getUid()       { return prefs.getString(KEY_UID,      ""); }
    public String  getPlan()      { return prefs.getString(KEY_PLAN,     "free"); }
    public String  getTrialEnd()  { return prefs.getString(KEY_TRIAL_END, ""); }
    public String  getSubEnd()    { return prefs.getString(KEY_SUB_END,   ""); }
    public String  getCreatedAt() { return prefs.getString(KEY_CREATED_AT, ""); }

    // ── Plan checks ──────────────────────────────────
    public boolean isPremium() {
        if (!"premium".equals(getPlan())) return false;
        String subEnd = getSubEnd();
        if (subEnd.isEmpty()) return false;
        try {
            long end = java.time.Instant.parse(subEnd).toEpochMilli();
            return System.currentTimeMillis() < end;
        } catch (Exception e) { return false; }
    }

    public boolean isTrialActive() {
        String trialEnd = getTrialEnd();
        if (trialEnd.isEmpty()) return false;
        try {
            long end = java.time.Instant.parse(trialEnd).toEpochMilli();
            return System.currentTimeMillis() < end;
        } catch (Exception e) { return false; }
    }

    public boolean hasAccess() {
        return isPremium() || isTrialActive();
    }

    public String getFirstName() {
        String name = getName();
        return name.contains(" ") ? name.split(" ")[0] : name;
    }
}
