package com.karatesan.game.ecs.systems.spawning;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;
import com.karatesan.game.ecs.factory.EnemyType;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.PausableSystem;
import com.karatesan.game.spawning.Formation;

public class WaveSpawnerSystem extends EntitySystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(
        TransformComponent.class);
    private final ComponentMapper<SessionComponent> sessionMapper = ComponentMapper.getFor(SessionComponent.class);

    private final EntityFactory entityFactory;
    private final GameContext context;
    private final GameConfig config;

    private final OrthographicCamera camera;

    private ImmutableArray<Entity> currentEnemies;
    private float spawnTimer = 0f;
    private int lastProcessedWave = -1;

    private final Vector2 spawnPos = new Vector2();
    private final float[] clusterX = new float[2];
    private final float[] clusterY = new float[2];

    // Current wave parameters — set from wave table on wave change
    private float currentSpawnInterval;
    private int currentSpawnCount;
    private int currentStandardPct;
    private int currentSwarmerPct;
    private int currentTankPct;
    private float currentHpScale;
    private Formation currentFormation = Formation.SCATTER;

    public WaveSpawnerSystem(EntityFactory entityFactory, GameContext context, GameConfig config,
                             OrthographicCamera camera) {
        this.entityFactory = entityFactory;
        this.context = context;
        this.config = config;
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        currentEnemies = engine.getEntitiesFor(
            Family.all(EnemyComponent.class).exclude(PendingRemovalComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Entity sessionEntity = context.getSession();
        Entity player = context.getPlayer();
        if (sessionEntity == null || player == null) return;

        SessionComponent session = sessionMapper.get(sessionEntity);

        if (session.currentWave > lastProcessedWave) {
            lastProcessedWave = session.currentWave;
            applyWaveDefinition(session.currentWave);
        }

        if (currentSpawnInterval <= 0f) return;

        spawnTimer += deltaTime;

        while (spawnTimer >= currentSpawnInterval) {
            spawnTimer -= currentSpawnInterval;

            int availableSlots = config.maxEnemiesOnScreen - currentEnemies.size();

            if (availableSlots <= 0) {
                spawnTimer = 0f;
                break;
            }
            rollFormation();
            spawnWave(player, Math.min(currentSpawnCount, availableSlots));
        }
    }

    // ── Wave Definition ─────────────────────────────────────────────

    private void applyWaveDefinition(int wave) {
        // Clamp to last entry — waves beyond the table repeat the final definition
        int index = Math.min(wave - 1, config.waves.size - 1);
        GameConfig.WaveEntry entry = config.waves.get(index);

        currentSpawnInterval = entry.spawnInterval;
        currentSpawnCount = entry.spawnCount;
        currentStandardPct = entry.standardPct;
        currentSwarmerPct = entry.swarmerPct;
        currentTankPct = entry.tankPct;
        currentHpScale = entry.hpScale;
    }

    private void rollFormation() {
        int roll = MathUtils.random(1, 100);

        int scatterCutoff = config.formationScatterWeight;
        int arcCutoff = scatterCutoff + config.formationArcWeight;
        int clusterCutoff = arcCutoff + config.formationClusterWeight;

        if (roll <= scatterCutoff) {
            currentFormation = Formation.SCATTER;
        } else if (roll <= arcCutoff) {
            currentFormation = Formation.ARC;
        } else if (roll <= clusterCutoff) {
            currentFormation = Formation.CLUSTER;
        } else {
            currentFormation = Formation.CIRCLE;
        }
    }

    // ── Spawning ────────────────────────────────────────────────────

    private void spawnWave(Entity player, int spawnCount) {
        TransformComponent pTransform = transformMapper.get(player);

        if (currentFormation == Formation.CLUSTER) {
            spawnClusterFormation(pTransform, spawnCount);
            return;
        }

        float baseAngle = MathUtils.random(0f, 360f);

        for (int i = 0; i < spawnCount; i++) {
            float angle = calculateSpawnAngle(i, baseAngle, spawnCount);
            Vector2 pos = calculateSpawnPosition(pTransform, angle);

            EnemyType type = rollEnemyType();
            entityFactory.createEnemy(pos.x, pos.y, type, currentHpScale);
        }
    }

    private Vector2 calculateSpawnPosition(TransformComponent playerTransform, float angle) {
        float dx = MathUtils.cosDeg(angle);
        float dy = MathUtils.sinDeg(angle);

        float halfWidth = camera.viewportWidth * camera.zoom * 0.5f;
        float halfHeight = camera.viewportHeight * camera.zoom * 0.5f;

        float left = camera.position.x - halfWidth - config.spawnMargin;
        float right = camera.position.x + halfWidth + config.spawnMargin;
        float bottom = camera.position.y - halfHeight - config.spawnMargin;
        float top = camera.position.y + halfHeight + config.spawnMargin;

        float distance = Float.MAX_VALUE;

        if (dx > 0f) {
            distance = Math.min(distance, (right - playerTransform.x) / dx);
        } else if (dx < 0f) {
            distance = Math.min(distance, (left - playerTransform.x) / dx);
        }

        if (dy > 0f) {
            distance = Math.min(distance, (top - playerTransform.y) / dy);
        } else if (dy < 0f) {
            distance = Math.min(distance, (bottom - playerTransform.y) / dy);
        }

        return spawnPos.set(playerTransform.x + dx * distance, playerTransform.y + dy * distance);
    }

    private void spawnClusterFormation(TransformComponent playerTransform, int spawnCount) {
        int clusterCount = spawnCount >= 6 ? 2 : 1;

        for (int i = 0; i < clusterCount; i++) {
            float angle = MathUtils.random(0f, 360f);
            Vector2 center = calculateSpawnPosition(playerTransform, angle);

            clusterX[i] = center.x;
            clusterY[i] = center.y;
        }

        for (int i = 0; i < spawnCount; i++) {
            int clusterIndex = i % clusterCount;

            float jitterAngle = MathUtils.random(0f, 360f);
            float jitterDistance = MathUtils.random(0f, config.clusterRadius);

            float x = clusterX[clusterIndex] + MathUtils.cosDeg(jitterAngle) * jitterDistance;
            float y = clusterY[clusterIndex] + MathUtils.sinDeg(jitterAngle) * jitterDistance;

            EnemyType type = rollEnemyType();
            entityFactory.createEnemy(x, y, type, currentHpScale);
        }
    }

    private float calculateSpawnAngle(int index, float baseAngle, int spawnCount) {
        return switch (currentFormation) {
            case SCATTER -> MathUtils.random(0f, 360f);
            case ARC -> {
                float offset = (index - (spawnCount - 1) / 2f) * config.arcStepAngle;
                yield baseAngle + offset;
            }
            case CIRCLE -> {
                float step = 360f / spawnCount;
                yield baseAngle + index * step;
            }
            case CLUSTER -> baseAngle; // unused, handled separately
        };
    }

    private EnemyType rollEnemyType() {
        int roll = MathUtils.random(1, 100);

        if (roll <= currentStandardPct) {
            return EnemyType.STANDARD;
        }
        if (roll <= currentStandardPct + currentSwarmerPct) {
            return EnemyType.SWARMER;
        }
        return EnemyType.TANK;
    }
}
