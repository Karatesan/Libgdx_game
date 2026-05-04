package com.karatesan.game.ecs.systems.spawning;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.factory.EnemyType;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.PausableSystem;
import com.karatesan.game.spawning.Formation;

public class WaveSpawnerSystem extends EntitySystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> transformMapper =
        ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<SessionComponent> sessionMapper =
        ComponentMapper.getFor(SessionComponent.class);

    private final EntityFactory entityFactory;
    private final GameContext context;
    private final GameConfig config;

    private ImmutableArray<Entity> currentEnemies;
    private float spawnTimer = 0f;
    private int lastProcessedWave = 0;

    // Current wave parameters — set from wave table on wave change
    private float currentSpawnInterval;
    private int currentSpawnCount;
    private int currentStandardPct;
    private int currentSwarmerPct;
    private int currentTankPct;
    private float currentHpScale;
    private Formation currentFormation = Formation.SCATTER;

    public WaveSpawnerSystem(EntityFactory entityFactory, GameContext context, GameConfig config) {
        this.entityFactory = entityFactory;
        this.context = context;
        this.config = config;
    }

    @Override
    public void addedToEngine(Engine engine) {
        currentEnemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Entity sessionEntity = context.getSession();
        Entity player = context.getPlayer();
        if (sessionEntity == null || player == null) return;

        SessionComponent session = sessionMapper.get(sessionEntity);

        // Check if SessionTimerSystem has advanced the wave
        if (session.currentWave > lastProcessedWave) {
            lastProcessedWave = session.currentWave;
            applyWaveDefinition(session.currentWave);
            rollFormation();
        }

        // Spawn logic with lag catch-up
        spawnTimer += deltaTime;
        while (spawnTimer >= currentSpawnInterval) {
            spawnTimer -= currentSpawnInterval;

            if (currentEnemies.size() >= config.maxEnemiesOnScreen) {
                spawnTimer = 0f;
                break;
            }

            spawnWave(player);
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

        if (roll <= scatterCutoff) {
            currentFormation = Formation.SCATTER;
        } else if (roll <= arcCutoff) {
            currentFormation = Formation.ARC;
        } else {
            currentFormation = Formation.CIRCLE;
        }
    }

    // ── Spawning ────────────────────────────────────────────────────

    private void spawnWave(Entity player) {
        TransformComponent pTransform = transformMapper.get(player);
        float baseAngle = MathUtils.random(0f, 360f);

        for (int i = 0; i < currentSpawnCount; i++) {
            float angle = calculateSpawnAngle(i, baseAngle);
            float x = pTransform.x + MathUtils.cosDeg(angle) * config.spawnRadius;
            float y = pTransform.y + MathUtils.sinDeg(angle) * config.spawnRadius;

            EnemyType type = rollEnemyType();
            entityFactory.createEnemy(x, y, type, currentHpScale);
        }
    }

    private float calculateSpawnAngle(int index, float baseAngle) {
        return switch (currentFormation) {
            case SCATTER -> MathUtils.random(0f, 360f);
            case ARC -> {
                float offset = (index - (currentSpawnCount - 1) / 2f) * config.arcStepAngle;
                yield baseAngle + offset;
            }
            case CIRCLE -> {
                float step = 360f / currentSpawnCount;
                yield baseAngle + (index * step);
            }
        };
    }

    private EnemyType rollEnemyType() {
        int roll = MathUtils.random(1, 100);
        if (roll <= currentTankPct) return EnemyType.TANK;
        if (roll <= currentTankPct + currentSwarmerPct) return EnemyType.SWARMER;
        return EnemyType.STANDARD;
    }
}
