package com.jaynes.maxtv.models;

public class Channel {
    public String name;
    public String category;
    public String url;
    public String image;
    public String key;       // ClearKey DRM kid:key
    public String type;      // HLS or DASH
    public String source;    // zimotv / bailatv / pixtvmax
    public boolean isFree;
    public boolean hasDrm;

    public Channel() {}

    public boolean isDash() {
        return "DASH".equalsIgnoreCase(type);
    }

    public boolean needsDrm() {
        return hasDrm && key != null && !key.isEmpty();
    }

    public String getKid() {
        if (key == null || !key.contains(":")) return "";
        return key.split(":")[0];
    }

    public String getDrmKey() {
        if (key == null || !key.contains(":")) return "";
        return key.split(":")[1];
    }
}
