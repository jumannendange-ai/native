package com.jaynes.maxtv;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * SessionManager — SharedPreferences auth storage
 * Stores: token, refresh_token, user_id, email, name, subscription
 */
public class SessionManager {

    private static final String PREF_NAME          = "jaynes_session";
    private static final String KEY_TOKEN          = "token";
    private static final String KEY_REFRESH        = "refresh_token";
    private static final String KEY_USER_ID        = "user_id";
    private static final String KEY_EMAIL          = "email";
    private static final String KEY_NAME           = "name";
    private static final String KEY_ROLE           = "role";
    private static final String KEY_SUB_ACTIVE     = "sub_active";
    private static final String KEY_SUB_PLAN       = "sub_plan";
    private static final String KEY_SUB_EXPIRES    = "sub_expires";
    private static final String KEY_LOGGED_IN      = "logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getApplicationContext()
                        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ── Save after login / register ────────────────────
    public void saveSession(JSONObject apiResponse) {
        try {
            String token        = apiResponse.optString("token", "");
            String refreshToken = apiResponse.optString("refresh_token", "");
            JSONObject user     = apiResponse.optJSONObject("user");
            JSONObject sub      = apiResponse.optJSONObject("subscription");

            editor.putString(KEY_TOKEN,   token);
            editor.putString(KEY_REFRESH, refreshToken);
            editor.putBoolean(KEY_LOGGED_IN, !token.isEmpty());

            if (user != null) {
                editor.putString(KEY_USER_ID, user.optString("id",    ""));
                editor.putString(KEY_EMAIL,   user.optString("email", ""));
                editor.putString(KEY_NAME,    user.optString("name",  ""));
                editor.putString(KEY_ROLE,    user.optString("role",  "user"));
            }

            if (sub != null) {
                editor.putBoolean(KEY_SUB_ACTIVE,  sub.optBoolean("active",     false));
                editor.putString(KEY_SUB_PLAN,     sub.optString("plan",        ""));
                editor.putString(KEY_SUB_EXPIRES,  sub.optString("expires_at",  ""));
            }

            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Update subscription status (from /api/auth me) ─
    public void updateSubscription(JSONObject sub) {
        if (sub == null) return;
        editor.putBoolean(KEY_SUB_ACTIVE,  sub.optBoolean("active",    false));
        editor.putString(KEY_SUB_PLAN,     sub.optString("plan",       ""));
        editor.putString(KEY_SUB_EXPIRES,  sub.optString("expires_at", ""));
        editor.apply();
    }

    // ── Getters ────────────────────────────────────────
    public boolean isLoggedIn()       { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public String  getToken()         { return prefs.getString(KEY_TOKEN,   ""); }
    public String  getRefreshToken()  { return prefs.getString(KEY_REFRESH, ""); }
    public String  getUserId()        { return prefs.getString(KEY_USER_ID, ""); }
    public String  getEmail()         { return prefs.getString(KEY_EMAIL,   ""); }
    public String  getName()          { return prefs.getString(KEY_NAME,    ""); }
    public String  getRole()          { return prefs.getString(KEY_ROLE,    "user"); }
    public boolean isSubscribed()     { return prefs.getBoolean(KEY_SUB_ACTIVE, false); }
    public String  getSubPlan()       { return prefs.getString(KEY_SUB_PLAN,    ""); }
    public String  getSubExpires()    { return prefs.getString(KEY_SUB_EXPIRES, ""); }
    public boolean isAdmin()          { return "admin".equals(getRole()); }

    // ── Logout / clear session ─────────────────────────
    public void clearSession() {
        editor.clear().apply();
    }
}
