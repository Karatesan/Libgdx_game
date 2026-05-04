package com.karatesan.game.ecs.systems.debug;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.Mappers;

import java.util.Arrays;

public class DebugDisplaySystem extends EntitySystem {

    private final SpriteBatch batch;
    private final OrthographicCamera uiCamera;
    private final GameContext context;
    private final BitmapFont font;

    private static final int FRAME_SAMPLE_COUNT = 300;
    private static final float RECALC_INTERVAL = 1.0f;
    private static final int MIN_ONE_PERCENT_SAMPLES = 3;

    private final float[] frameSamples = new float[FRAME_SAMPLE_COUNT];
    private final float[] sortBuffer = new float[FRAME_SAMPLE_COUNT];
    private int sampleIndex = 0;
    private int sampleCount = 0;
    private float recalcTimer = 0f;
    private float cachedOnePercentLowMs = 0f;
    private float cachedOnePercentLowFps = 0f;

    public DebugDisplaySystem(SpriteBatch batch, GameContext context, GameConfig config) {
        super(Integer.MAX_VALUE);

        this.batch = batch;
        this.context = context;

        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, config.viewportWidth, config.viewportHeight);

        this.font = new BitmapFont();
        this.font.getData().markupEnabled = false;
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            DebugDisplay.toggle();
        }

        if (!DebugDisplay.isEnabled()) {
            DebugDisplay.clear();
            return;
        }

        DebugDisplay.tick(deltaTime);
        trackFrameTime(deltaTime);
        logBuiltInEntries(deltaTime);
        render();
    }

    // ── Built-in Entries ────────────────────────────────

    private void logBuiltInEntries(float deltaTime) {
        float frameMs = deltaTime * 1000f;
        float avgMs = averageFrameTime() * 1000f;

        DebugDisplay.log("FPS", Gdx.graphics.getFramesPerSecond());
        DebugDisplay.logCompound("Frame", frameMs, " ms (avg ", avgMs, " ms)");
        DebugDisplay.logCompound("1% Low", cachedOnePercentLowFps, " FPS (", cachedOnePercentLowMs, " ms)");

        Engine engine = getEngine();
        if (engine != null) {
            DebugDisplay.log("Entities", engine.getEntities().size());
            DebugDisplay.log("Enemies",
                engine.getEntitiesFor(Family.all(EnemyComponent.class).get()).size());
            DebugDisplay.log("Bullets",
                engine.getEntitiesFor(Family.all(BulletComponent.class).get()).size());
        }

        Entity player = context.getPlayer();
        if (player != null) {
            HealthComponent hp = Mappers.health.get(player);
            if (hp != null) {
                DebugDisplay.logCompound("HP", (int) hp.currentHp, " / ", (int) hp.maxHp);
            }
        }

        Entity sessionEntity = context.getSession();
        if (sessionEntity != null) {
            SessionComponent session = Mappers.session.get(sessionEntity);
            DebugDisplay.log("Wave", session.currentWave);
            DebugDisplay.log("Time", session.timeSurvived, "s");
        }
    }

    // ── Frame Time Tracking ─────────────────────────────

    private void trackFrameTime(float deltaTime) {
        frameSamples[sampleIndex] = deltaTime;
        sampleIndex = (sampleIndex + 1) % FRAME_SAMPLE_COUNT;
        sampleCount = Math.min(sampleCount + 1, FRAME_SAMPLE_COUNT);

        recalcTimer += deltaTime;
        if (recalcTimer >= RECALC_INTERVAL && sampleCount >= 10) {
            recalcTimer = 0f;
            calculateOnePercentLow();
        }
    }

    private void calculateOnePercentLow() {
        System.arraycopy(frameSamples, 0, sortBuffer, 0, sampleCount);
        Arrays.sort(sortBuffer, 0, sampleCount);

        int bucketSize = Math.max(MIN_ONE_PERCENT_SAMPLES, sampleCount / 100);
        float sum = 0f;
        for (int i = sampleCount - bucketSize; i < sampleCount; i++) {
            sum += sortBuffer[i];
        }

        float avgSeconds = sum / bucketSize;
        cachedOnePercentLowMs = avgSeconds * 1000f;
        cachedOnePercentLowFps = avgSeconds > 0 ? 1f / avgSeconds : 0f;
    }

    private float averageFrameTime() {
        if (sampleCount == 0) return 0f;
        float sum = 0f;
        for (int i = 0; i < sampleCount; i++) {
            sum += frameSamples[i];
        }
        return sum / sampleCount;
    }

    // ── Rendering ───────────────────────────────────────

    private void render() {
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        float x = 10f;
        float y = uiCamera.viewportHeight - 10f;
        float lineHeight = font.getLineHeight() + 2f;

        // Permanent entries (green)
        for (ObjectMap.Entry<String, String> entry : DebugDisplay.entries()) {
            drawLine(entry.key + ": " + entry.value, x, y, 0f, 1f, 0f);
            y -= lineHeight;
        }

        // Timed entries (yellow)
        for (ObjectMap.Entry<String, DebugDisplay.TimedEntry> entry : DebugDisplay.timedEntries()) {
            drawLine(entry.key + ": " + entry.value.value, x, y, 1f, 1f, 0f);
            y -= lineHeight;
        }

        batch.end();
    }

    private void drawLine(String line, float x, float y, float r, float g, float b) {
        font.setColor(0f, 0f, 0f, 0.8f);
        font.draw(batch, line, x + 1f, y - 1f);
        font.setColor(r, g, b, 1f);
        font.draw(batch, line, x, y);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        font.dispose();
    }
}
