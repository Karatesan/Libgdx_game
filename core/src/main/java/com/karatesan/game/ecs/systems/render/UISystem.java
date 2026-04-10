package com.karatesan.game.ecs.systems.render;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.utility.State;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UISystem extends IteratingSystem {

    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeDrawer shapeDrawer;
    private final Viewport viewport;
    private Entity sessionEntity;
    private final CharArray UIText = new CharArray();

    public UISystem(SpriteBatch batch, BitmapFont font, ShapeDrawer shapeDrawer, Viewport viewport) {
        super(Family.all(PlayerComponent.class, HealthComponent.class).get());
        this.batch = batch;
        this.shapeDrawer = shapeDrawer;
        this.font = font;
        this.viewport = viewport;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    public void update(float deltaTime) {
        // 1. Get the dynamic width and height of the screen
        float screenW = viewport.getWorldWidth();
        float screenH = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        super.update(deltaTime); // Draws Player HP

        SessionComponent session = sm.get(sessionEntity);

        // --- DRAW KILL COUNT (Anchored to Top-Right) ---
        UIText.clear();
        UIText.append("Killed: ").append(session.kilLCount);
        font.getData().setScale(.5f);
        // screenW - 150 pixels from the right edge, screenH - 20 pixels from the top
        font.draw(batch, UIText, screenW - 150f, screenH - 20f);
        font.getData().setScale(1f);

        // --- DRAW CLOCK (Top Center) ZERO GC ---
        int totalSeconds = (int) session.timeSurvived;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        UIText.clear();
        UIText.append(minutes).append(":");
        if (seconds < 10) UIText.append("0"); // Add leading zero (e.g., 1:05)
        UIText.append(seconds);
        font.getData().setScale(.5f); // Make it big!

        // screenW / 2 finds the exact dead center of the screen
        font.draw(batch, UIText, (screenW / 2f) - 20f, screenH - 20f);
        font.getData().setScale(1f); // Make it big!

        // --- DRAW WAVE ANNOUNCEMENT (Anchored to Dead Center) ---
        if (session.waveTextTimer > 0) {
            UIText.clear();
            UIText.append("WAVE ").append(session.currentWave);

            font.setColor(Color.YELLOW);
            // Center it perfectly in the middle of the screen
            font.draw(batch, UIText, (screenW / 2f) - 60f, (screenH / 2f) + 50f);

            font.setColor(Color.WHITE);
            font.getData().setScale(1f);
        }

        // --- DRAW GAME OVER ---
        if (session.currentState == State.GAME_OVER) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", (screenW / 2f) - 60f, (screenH / 2f) + 50f);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press 'R' to Restart", (screenW / 2f) - 160f, (screenH / 2f));
        }

        // --- DRAW XP BAR (Anchored to Top-Center, just below the clock) ---
        float barWidth = screenW;
        float barHeight = 5f;
        // Center the bar horizontally
        float barX = 0;
        // Place it 50 pixels down from the top edge
        float barY = screenH - barHeight;

        float fillRatio = session.currentXp / session.xpToNextLevel;

        shapeDrawer.setColor(Color.DARK_GRAY);
        shapeDrawer.filledRectangle(barX, barY, barWidth, barHeight);
        shapeDrawer.setColor(Color.CYAN);
        shapeDrawer.filledRectangle(barX, barY, barWidth * fillRatio, barHeight);

//        UIText.clear();
//        UIText.append("LVL ").append(session.currentLevel);
//        font.setColor(Color.WHITE);
//        // Draw the level text just to the left of the XP bar
//        font.draw(batch, UIText, barX - 60f, barY + 12f);
        batch.end();
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        HealthComponent health = hm.get(player);
        float screenH = viewport.getWorldHeight();

        UIText.clear();
        UIText.append("HP: ").append((int) health.currentHp).append("/").append((int) health.maxHp);
        font.getData().setScale(.5f);
        // Anchored to Top-Left (20 pixels from left edge, 20 pixels from top edge)
        font.draw(batch, UIText, 20f, screenH - 20f);
        font.getData().setScale(1f);

    }
}
