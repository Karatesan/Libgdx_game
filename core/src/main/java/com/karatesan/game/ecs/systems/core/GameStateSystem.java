package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.combat.InvincibilityComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.event.StatsRecalculationFlag;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.utility.PausableSystem;
import com.karatesan.game.ecs.utility.State;

public class GameStateSystem extends EntitySystem {

    private final GameContext context;
    private final GameConfig config;

    private final ComponentMapper<SessionComponent> sessionMapper = ComponentMapper.getFor(SessionComponent.class);
    private final ComponentMapper<FatalDamageComponent> fatalDamageMapper = ComponentMapper.getFor(
        FatalDamageComponent.class);
    private final ComponentMapper<LevelUpComponent> levelUpMapper = ComponentMapper.getFor(LevelUpComponent.class);
    private final ComponentMapper<HealthComponent> healthMapper = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(
        TransformComponent.class);

    public GameStateSystem(GameContext context, GameConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void update(float deltaTime) {
        Entity sessionEntity = context.getSession();
        Entity player = context.getPlayer();
        if (sessionEntity == null || player == null) return;

        SessionComponent session = sessionMapper.get(sessionEntity);

        switch (session.currentState) {
            case PLAYING -> handlePlaying(deltaTime, session, player);
            case GAME_OVER -> handleGameOver(session);
            case LEVEL_UP -> handleLevelUp(session, player);
        }
    }

    // ── State Handlers ──────────────────────────────────────────────

    private void handlePlaying(float deltaTime, SessionComponent session, Entity player) {
        // State exit checks — priority ordered
        if (fatalDamageMapper.has(player)) {
            transitionTo(State.GAME_OVER, session);
            return;
        }

        if (levelUpMapper.has(player)) {
            transitionTo(State.LEVEL_UP, session);
            return;
        }
    }

    private void handleGameOver(SessionComponent session) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetRun(session);
            transitionTo(State.PLAYING, session);
        }
    }

    private void handleLevelUp(SessionComponent session, Entity player) {
        // LevelUpComponent is removed by the perk selection UI/system
        // once the player has made their choice
        if (!levelUpMapper.has(player)) {
            transitionTo(State.PLAYING, session);
        }
    }

    // ── State Transition ────────────────────────────────────────────

    private void transitionTo(State newState, SessionComponent session) {
        session.currentState = newState;

        boolean shouldProcess = (newState == State.PLAYING);
        for (EntitySystem system : getEngine().getSystems()) {
            if (system instanceof PausableSystem) {
                system.setProcessing(shouldProcess);
            }
        }
    }

    // ── Run Reset ───────────────────────────────────────────────────

    private void resetRun(SessionComponent session) {
        Entity player = context.getPlayer();
        PooledEngine engine = (PooledEngine) getEngine();

        resetPlayer(player);
        resetSession(session);
        clearEntities(engine);
    }

    private void resetPlayer(Entity player) {
        // Clear death and invincibility state
        player.remove(FatalDamageComponent.class);
        player.remove(InvincibilityComponent.class);

        // Clear all perks
        PerkInventoryComponent inv = Mappers.perks.get(player);
        inv.acquiredPerks.clear();

        // Trigger full stat rebuild from base
        if (!Mappers.recalcFlag.has(player)) {
            player.add(((PooledEngine) getEngine()).createComponent(StatsRecalculationFlag.class));
        }

        // Restore health
        HealthComponent health = healthMapper.get(player);
        health.currentHp = health.maxHp;

        // Center on screen
        TransformComponent transform = transformMapper.get(player);
        transform.x = config.viewportWidth / 2f;
        transform.y = config.viewportHeight / 2f;
    }

    private void resetSession(SessionComponent session) {
        session.timeSurvived = 0f;
        session.currentWave = 1;
        session.waveTextTimer = config.waveTextDuration;
    }

    private void clearEntities(PooledEngine engine) {
        markForDeath(engine, Family.all(EnemyComponent.class).get());
        markForDeath(engine, Family.all(BulletComponent.class).get());
    }

    private void markForDeath(PooledEngine engine, Family family) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).add(engine.createComponent(DeadComponent.class));
        }
    }
}
