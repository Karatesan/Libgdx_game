package com.karatesan.game.ecs.systems.waveSystem;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.SessionComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.factory.EnemyType;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.State;

public class WaveSpawnerSystem extends EntitySystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private final EntityFactory entityFactory;
    private ImmutableArray<Entity> sessionEntities;
    private ImmutableArray<Entity> playerEntities;
    private ImmutableArray<Entity> currentEnemies;

    private final int MAX_ENEMIES_ON_SCREEN = 300;
    private float spawnTimer = 0f;

    // Track the wave internally to know when to recalculate
    private int lastProcessedWave = 0;

    private float currentSpawnRate = 1.0f;
    private int currentSpawnAmount = 1;
    private Formation currentFormation = Formation.SCATTER;

    public WaveSpawnerSystem(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    public void addedToEngine(Engine engine) {
        // Safely track entities without assuming they exist immediately
        sessionEntities = engine.getEntitiesFor(Family.all(SessionComponent.class).get());
        playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class, TransformComponent.class).get());
        currentEnemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (sessionEntities.size() == 0 || playerEntities.size() == 0) return;

        SessionComponent session = sm.get(sessionEntities.first());
        if (session.currentState != State.PLAYING) return;

        // NOTE: timeSurvived and waveTextTimer should be updated in a SessionSystem!

        // 1. Check if the SessionSystem has advanced the wave
        if (session.currentWave > lastProcessedWave) {
            lastProcessedWave = session.currentWave;
            recalculateWaveRules(session.currentWave);
        }

        // 2. SPAWN LOGIC
        spawnTimer += deltaTime;

        // Use a while loop to catch up on lag, but break if we hit the cap
        while (spawnTimer >= currentSpawnRate) {
            spawnTimer -= currentSpawnRate;

            if (currentEnemies.size() >= MAX_ENEMIES_ON_SCREEN) {
                // Consume the rest of the timer so we don't build "spawn debt"
                spawnTimer = 0f;
                break;
            }

            spawnEnemies(session);
        }
    }

    private void recalculateWaveRules(int wave) {
        // Gentler curve: Reaches 0.2s cap at Wave 20 instead of Wave 7
        currentSpawnRate = Math.max(0.2f, 1.2f - (wave * 0.05f));

        // Slower amount scaling
        currentSpawnAmount = 1 + (wave / 4);

        int randomForm = MathUtils.random(0, 10);
        // 70% chance for scatter, 20% for Arc, 10% for Circle (Makes circles special)
        if (randomForm < 7) currentFormation = Formation.SCATTER;
        else if (randomForm < 9) currentFormation = Formation.ARC;
        else currentFormation = Formation.CIRCLE;
    }

    private void spawnEnemies(SessionComponent session) {
        Entity player = playerEntities.first();
        TransformComponent pTransform = tm.get(player);

        // TODO: Replace 400f with (CameraViewportWidth / 2) + 50f padding
        float radius = 600f;
        float baseAngle = MathUtils.random(0f, 360f);

        for (int i = 0; i < currentSpawnAmount; i++) {
            float angle = switch (currentFormation) {
                case SCATTER -> MathUtils.random(0f, 360f);
                case ARC -> {
                    // Centers the arc on the baseAngle
                    float arcStep = 15f;
                    float offset = (i - (currentSpawnAmount - 1) / 2f) * arcStep;
                    yield baseAngle + offset;
                }
                case CIRCLE -> {
                    float angleStep = 360f / currentSpawnAmount;
                    yield baseAngle + (i * angleStep);
                }
            };

            float x = pTransform.x + MathUtils.cosDeg(angle) * radius;
            float y = pTransform.y + MathUtils.sinDeg(angle) * radius;

            EnemyType typeToSpawn = rollEnemyType(session.currentWave);
            entityFactory.createEnemy(x, y, typeToSpawn);
        }
    }

    private EnemyType rollEnemyType(int currentWave) {
        int roll = MathUtils.random(1, 100);

        if (currentWave == 1) {
            // Minute 1: 100% Standard enemies. Let the player learn the controls.
            return EnemyType.STANDARD;
        }
        else if (currentWave == 2) {
            // Minute 2: Introduce Swarmers! (70% Standard, 30% Swarmers)
            if (roll <= 30) return EnemyType.SWARMER;
            return EnemyType.STANDARD;
        }
        else {
            // Minute 3+: Total War. (50% Standard, 35% Swarmers, 15% Tanks)
            if (roll <= 15) return EnemyType.TANK;
            if (roll <= 50) return EnemyType.SWARMER; // 16 to 50 is 35%
            return EnemyType.STANDARD;
        }
    }
}
