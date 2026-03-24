package com.jaynes.maxtv;

/**
 * Channel — Data model kutoka /api/channels/all
 */
public class Channel {
    public String name;
    public String category;
    public String url;
    public String image;
    public String key;       // ClearKey kid:key
    public String type;      // HLS | DASH
    public String source;    // zimotv | bailatv | pixtvmax
    public boolean isFree;
    public boolean hasDrm;

    public Channel() {}

    public Channel(String name, String category, String url, String image,
                   String key, String type, String source, boolean isFree, boolean hasDrm) {
        this.name     = name;
        this.category = category;
        this.url      = url;
        this.image    = image;
        this.key      = key;
        this.type     = type;
        this.source   = source;
        this.isFree   = isFree;
        this.hasDrm   = hasDrm;
    }

    /** Parse from JSONObject */
    public static Channel fromJson(org.json.JSONObject j) {
        Channel ch = new Channel();
        ch.name     = j.optString("name",     "");
        ch.category = j.optString("category", "OTHER CHANNELS");
        ch.url      = j.optString("url",      "");
        ch.image    = j.optString("image",    "");
        ch.key      = j.optString("key",      "");
        ch.type     = j.optString("type",     "HLS");
        ch.source   = j.optString("source",   "");
        ch.isFree   = j.optBoolean("is_free", false);
        ch.hasDrm   = j.optBoolean("has_drm", false);
        return ch;
    }

    public boolean isValid() {
        return !url.isEmpty() && !name.isEmpty();
    }
}
