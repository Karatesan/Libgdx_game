package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.ecs.components.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.combat.FatalDamageComponent;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.systems.waveSystem.WaveSpawnerSystem;
import com.karatesan.game.ecs.utility.State;

public class GameStateSystem extends EntitySystem {
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);
    private float waveChangeTime = 60f; // Alternatively, put this inside SessionComponent


    private Entity sessionEntity;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Find the one and only Session Entity and cache it!
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    public void update(float deltaTime) {
        SessionComponent session = sm.get(sessionEntity);
        PooledEngine engine = (PooledEngine) getEngine();

        if (session.currentState == State.PLAYING) {
            // Check if the player was tagged with FatalDamage this frame
            ImmutableArray<Entity> deadPlayers = engine.getEntitiesFor(Family.all(FatalDamageComponent.class).get());
            if (deadPlayers.size() > 0) {
                setGameState(State.GAME_OVER, session);
            }
            //updates for wave manager
            if (session.waveTextTimer > 0) {
                session.waveTextTimer -= deltaTime;
            }
            session.timeSurvived += deltaTime;

            int calculatedWave = (int) (session.timeSurvived / waveChangeTime) + 1;
            if (calculatedWave > session.currentWave) {
                session.currentWave = calculatedWave;
                session.waveTextTimer = 3f; // Trigger the UI text
            }
        } else if (session.currentState == State.GAME_OVER) {
            // Listen for the Restart button
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame(engine, session);
            }
        }
    }

    private void setGameState(State newState, SessionComponent session) {
        session.currentState = newState;

        // If Game Over, turn OFF all gameplay systems. If Playing, turn them ON.
        boolean isPlaying = (newState == State.PLAYING);

        PooledEngine engine = (PooledEngine) getEngine();
        engine.getSystem(PlayerInputSystem.class).setProcessing(isPlaying);
        engine.getSystem(WeaponSystem.class).setProcessing(isPlaying);
        engine.getSystem(EnemySystem.class).setProcessing(isPlaying);
        engine.getSystem(MovementSystem.class).setProcessing(isPlaying);
        engine.getSystem(WaveSpawnerSystem.class).setProcessing(isPlaying);
        engine.getSystem(CollisionSystem.class).setProcessing(isPlaying);
        engine.getSystem(BulletSystem.class).setProcessing(isPlaying);

        // Notice we do NOT pause RenderSystem, UISystem, or CleanupSystem!
    }

    private void restartGame(PooledEngine engine, SessionComponent session) {
        // 1. Restore the Player
        Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
        player.remove(FatalDamageComponent.class);
        player.remove(InvincibilityComponent.class); // Clear i-frames just in case

        HealthComponent health = player.getComponent(HealthComponent.class);
        health.currentHp = health.maxHp;

        TransformComponent transform = player.getComponent(TransformComponent.class);
        transform.x = 400f; // Center of 800x600 viewport
        transform.y = 300f;

        // 2. Wipe the Board (Kill all enemies and bullets)
        ImmutableArray<Entity> enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).add(engine.createComponent(DeadComponent.class));
        }

        ImmutableArray<Entity> bullets = engine.getEntitiesFor(Family.all(BulletComponent.class).get());
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).add(engine.createComponent(DeadComponent.class));
        }

        // 3. Resume the Game!
        setGameState(State.PLAYING, session);
    }
}
