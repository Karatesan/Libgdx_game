package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.CharArray;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.SessionComponent;
import com.karatesan.game.ecs.utility.State;

public class UISystem extends IteratingSystem {

    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera uiCamera;
    private Entity sessionEntity;
    private final CharArray UIText = new CharArray();

    public UISystem(SpriteBatch batch, BitmapFont font, OrthographicCamera uiCamera) {
        super(Family.all(PlayerComponent.class, HealthComponent.class).get());
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        super.update(deltaTime); // Draws Player HP

        SessionComponent session = sm.get(sessionEntity);

        // --- DRAW KILL COUNT (Top Right) ---
        UIText.clear();
        UIText.append("Killed: ").append(session.kilLCount);
        font.getData().setScale(.5f);
        font.draw(batch, UIText, 700, 580);
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

        // Assuming 800x600 screen, center is ~380
        font.draw(batch, UIText, 380, 580);
        font.getData().setScale(1f); // Make it big!

        // --- DRAW WAVE ANNOUNCEMENT ---
        if (session.waveTextTimer > 0) {
            UIText.clear();
            UIText.append("WAVE ").append(session.currentWave);

            // Make it yellow and draw it in the center of the screen
            font.setColor(Color.YELLOW);
            font.draw(batch, UIText, 340, 400);

            // Reset font settings
            font.setColor(Color.WHITE);
        }

        // --- DRAW GAME OVER ---
        if (session.currentState == State.GAME_OVER) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 320, 350);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press 'R' to Restart", 280, 300);
        }

        batch.end();
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        HealthComponent health = hm.get(player);
        UIText.clear();
        UIText.append("HP: ").append((int) health.currentHp).append("/").append((int) health.maxHp);
        font.getData().setScale(.5f);
        font.draw(batch, UIText, 20, 580);
        font.getData().setScale(1f);

    }
}
