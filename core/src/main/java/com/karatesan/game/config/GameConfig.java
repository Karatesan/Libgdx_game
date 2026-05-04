package com.karatesan.game.config;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class GameConfig {

    // ── Display ─────────────────────────────────────────
    public float viewportWidth = 800f;
    public float viewportHeight = 600f;

    // ── Wave System ─────────────────────────────────────
    public float waveChangeTime = 60f;
    public float waveTextDuration = 3f;
    public int maxEnemiesOnScreen = 300;
    public float spawnRadius = 600f;

    // ── Wave Formations ─────────────────────────────────
    public float arcStepAngle = 15f;
    public int formationScatterWeight = 70;
    public int formationArcWeight = 20;
    // Circle weight is implicit: 100 - scatter - arc

    // ── Wave Definitions ────────────────────────────────
    // One entry per wave. Last entry repeats for all subsequent waves.
    public Array<WaveEntry> waves = new Array<>();

    // ── Combat ──────────────────────────────────────────
    public float iFramesDuration = 0.40f;
    public float fireRateFloor = 0.08f;
    public float fireRateCeiling = 3.0f;
    public float critChanceFloor = 0.f;
    public float critChanceCeiling = 0.8f;

    public float bulletSize = 4f;

    // ── Pullable ──────────────────────────────────────────
    public float pullSpeed = 220f;

    // ── Enemy Behavior ──────────────────────────────────
    public float separationForce = 150f;

    // ── Progression ─────────────────────────────────────
    public float xpFormulaBase = 20f;
    public float xpFormulaExponent = 1.35f;

    // ── Projectile Mechanics ────────────────────────────
    public float pierceDamageRetention = 0.85f;
    public float ricochetBaseRetention = 0.85f;
    public float ricochetMaxBounceRange = 250f;
    public float explosionRadius = 60f;
    public float explosionDamageRatio = 0.50f;

    private GameConfig() {}

    public static GameConfig load(FileHandle file) {
        if (file != null && file.exists()) {
            return new Json().fromJson(GameConfig.class, file);
        }
        return new GameConfig();
    }

    public static GameConfig defaults() {
        GameConfig c = new GameConfig();
        c.waves.add(new WaveEntry(1.40f, 1, 100,  0,  0, 1.00f));
        c.waves.add(new WaveEntry(1.20f, 1,  70, 30,  0, 1.00f));
        c.waves.add(new WaveEntry(1.00f, 2,  55, 40,  5, 1.00f));
        c.waves.add(new WaveEntry(0.90f, 2,  50, 35, 15, 1.10f));
        c.waves.add(new WaveEntry(0.75f, 3,  40, 40, 20, 1.15f));
        c.waves.add(new WaveEntry(0.70f, 3,  35, 45, 20, 1.20f));
        c.waves.add(new WaveEntry(0.65f, 3,  30, 45, 25, 1.30f));
        c.waves.add(new WaveEntry(0.55f, 4,  25, 50, 25, 1.40f));
        c.waves.add(new WaveEntry(0.50f, 4,  25, 45, 30, 1.50f));
        c.waves.add(new WaveEntry(0.45f, 5,  20, 50, 30, 1.65f));
        c.waves.add(new WaveEntry(0.40f, 5,  20, 50, 30, 1.80f));
        c.waves.add(new WaveEntry(0.35f, 6,  15, 50, 35, 2.00f));
        c.waves.add(new WaveEntry(0.35f, 6,  15, 45, 40, 2.25f));
        c.waves.add(new WaveEntry(0.35f, 7,  10, 50, 40, 2.50f));
        c.waves.add(new WaveEntry(0.35f, 7,  10, 50, 40, 2.80f));
        return c;
    }

    public static class WaveEntry {

        public float spawnInterval;
        public int spawnCount;
        public int standardPct;
        public int swarmerPct;
        public int tankPct;
        public float hpScale;

        public WaveEntry() {}

        public WaveEntry(float spawnInterval, int spawnCount,
                         int standardPct, int swarmerPct, int tankPct,
                         float hpScale) {
            this.spawnInterval = spawnInterval;
            this.spawnCount = spawnCount;
            this.standardPct = standardPct;
            this.swarmerPct = swarmerPct;
            this.tankPct = tankPct;
            this.hpScale = hpScale;
        }
    }
}
