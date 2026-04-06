package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
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
    private CharArray UIText = new CharArray();

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Find the one and only Session Entity and cache it!
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    // We pass in the tools from Main and the uiCamera from FirstScreen
    public UISystem(SpriteBatch batch, BitmapFont font, OrthographicCamera uiCamera) {
        // This system only cares about the Player (to read their health)
        super(Family.all(PlayerComponent.class, HealthComponent.class).get());
        this.batch = batch;
        this.font = font;
        this.uiCamera = uiCamera;
    }

    @Override
    public void update(float deltaTime) {
        // 1. Tell the batch to use the UI Camera's static coordinates
        batch.setProjectionMatrix(uiCamera.combined);

        // 2. Begin drawing the HUD
        batch.begin();

        // 3. This triggers processEntity() to find the player and draw their stats
        super.update(deltaTime);

        SessionComponent session = sm.get(sessionEntity);
        UIText.clear();
        UIText.append("Killed: ").append(session.kilLCount);
        font.draw(batch, UIText, 700, 580);

        if (session.currentState == State.GAME_OVER) {
            // Hardcoded coordinates to roughly center the text on an 800x600 screen
            font.draw(batch, "GAME OVER", 320, 350);
            font.draw(batch, "Press 'R' to Restart", 280, 300);
        }
        // 4. Finish drawing
        batch.end();
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        HealthComponent health = hm.get(player);
        // X: 20 (left edge), Y: 580 (near the top of our 800x600 viewport)
        UIText.clear();
        UIText.append("HP: ").append((int) health.currentHp).append("/").append((int) health.maxHp);
        font.draw(batch, UIText, 20, 580);
    }
}
