package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.combat.InvincibilityComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.utility.State;

public class GameStateSystem extends EntitySystem {
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);
    private final ComponentMapper<FatalDamageComponent> fatalDamageMap = ComponentMapper.getFor(
        FatalDamageComponent.class);
    private final ComponentMapper<LevelUpComponent> levelUpMap = ComponentMapper.getFor(LevelUpComponent.class);

    private float waveChangeTime = 60f; // Alternatively, put this inside SessionComponent

    private Entity sessionEntity;
    private Entity player;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Find the one and only Session Entity and cache it!
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
        player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
    }
//TODO refactor this to switch
    @Override
    public void update(float deltaTime) {
        SessionComponent session = sm.get(sessionEntity);
        PooledEngine engine = (PooledEngine) getEngine();

        if (session.currentState == State.PLAYING) {
            //check game state -----------------------------------------------------
            if (fatalDamageMap.has(player)) {
                setGameStateAndPause(State.GAME_OVER, session, engine);
            } else if (player.getComponent(LevelUpComponent.class) != null) {
                setGameStateAndPause(State.LEVEL_UP, session, engine);
            }
            //updates for wave manager -----------------------------------------------
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
                restartGame(session, engine);
            }
        } else if (session.currentState == State.LEVEL_UP) {
            if (player.getComponent(LevelUpComponent.class) == null) {
                setGameStateAndStart(State.PLAYING, session, engine);
            }
        }
    }

    private void setGameStateAndPause(State newState, SessionComponent session, PooledEngine engine) {
        session.currentState = newState;
        pauseSystems(engine);
    }

    private void setGameStateAndStart(State newState, SessionComponent session, PooledEngine engine) {
        session.currentState = newState;
        startSystems(engine);
    }

    private void pauseSystems(PooledEngine engine) {
        changeSystemsState(false, engine);
    }

    private void startSystems(PooledEngine engine) {
        changeSystemsState(true, engine);
    }

    private void changeSystemsState(boolean newState, PooledEngine engine) {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof PausableSystem) system.setProcessing(newState);
        }
    }

    private void restartGame(SessionComponent session, PooledEngine engine) {
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
        setGameStateAndStart(State.PLAYING, session, engine);
    }
}
