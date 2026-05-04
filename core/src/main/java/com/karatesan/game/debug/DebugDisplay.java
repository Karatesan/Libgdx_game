package com.karatesan.game.debug;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import java.util.Iterator;

public final class DebugDisplay {

    private static final OrderedMap<String, String> entries = new OrderedMap<>();
    private static final OrderedMap<String, TimedEntry> timedEntries = new OrderedMap<>();
    private static final StringBuilder sb = new StringBuilder(64);
    private static boolean enabled = true;

    private DebugDisplay() {}

    // ── Timed entry holder ──────────────────────────────

    public static class TimedEntry {
        public String value;
        float remaining;
        TimedEntry(String value, float remaining) {
            this.value = value;
            this.remaining = remaining;
        }
    }

    // ── Simple types ────────────────────────────────────

    public static void log(String key, String value) {
        if (enabled) entries.put(key, value);
    }

    public static void log(String key, int value) {
        if (enabled) entries.put(key, Integer.toString(value));
    }

    public static void log(String key, boolean value) {
        if (enabled) entries.put(key, Boolean.toString(value));
    }

    // ── Float with decimals ─────────────────────────────

    public static void log(String key, float value) {
        if (!enabled) return;
        sb.setLength(0);
        appendFloat(sb, value, 2);
        entries.put(key, sb.toString());
    }

    public static void log(String key, float value, String suffix) {
        if (!enabled) return;
        sb.setLength(0);
        appendFloat(sb, value, 1);
        sb.append(suffix);
        entries.put(key, sb.toString());
    }

    // ── Compound entries ────────────────────────────────

    public static void logCompound(String key, Object... parts) {
        if (!enabled) return;
        sb.setLength(0);
        for (Object part : parts) {
            if (part instanceof Float f) {
                appendFloat(sb, f, 1);
            } else {
                sb.append(part);
            }
        }
        entries.put(key, sb.toString());
    }

    // ── Timed entries ───────────────────────────────────

    public static void logTimed(String key, String value, float seconds) {
        if (enabled) timedEntries.put(key, new TimedEntry(value, seconds));
    }

    public static void tick(float deltaTime) {
        Iterator<ObjectMap.Entry<String, TimedEntry>> it = timedEntries.iterator();
        while (it.hasNext()) {
            ObjectMap.Entry<String, TimedEntry> entry = it.next();
            entry.value.remaining -= deltaTime;
            if (entry.value.remaining <= 0f) it.remove();
        }
    }

    public static OrderedMap<String, TimedEntry> timedEntries() { return timedEntries; }

    // ── Float formatting without String.format ──────────

    private static void appendFloat(StringBuilder sb, float value, int decimals) {
        if (value < 0) {
            sb.append('-');
            value = -value;
        }
        int whole = (int) value;
        sb.append(whole).append('.');
        float frac = value - whole;
        for (int i = 0; i < decimals; i++) {
            frac *= 10;
            sb.append((int) frac % 10);
        }
    }

    // ── Control ─────────────────────────────────────────

    public static void toggle() { enabled = !enabled; }

    public static boolean isEnabled() { return enabled; }

    public static OrderedMap<String, String> entries() { return entries; }

    public static void clear() {
        entries.clear();
        timedEntries.clear();
    }
}
