package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.SessionComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.ECSUtils;
import com.karatesan.game.ecs.utility.State;

public class WaveSpawnerSystem extends EntitySystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private float spawnTimer = 0f;
    private Entity playerEntity;
    private Entity sessionEntity;
    private final EntityFactory entityFactory;

    public WaveSpawnerSystem(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    public void addedToEngine(Engine engine) {
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    public void update(float deltaTime) {
        SessionComponent session = sm.get(sessionEntity);

        if (session.currentState != State.PLAYING) return;

        if (session.waveTextTimer > 0) {
            session.waveTextTimer -= deltaTime;
        }

        // 1. Update the Master Clock
        session.timeSurvived += deltaTime;

        // 2. Calculate Current Wave (1 new wave every 60 seconds)
        int calculatedWave = (int) (session.timeSurvived / 60f) + 1;

        // 3. Trigger Wave Announcement if it changed
        if (calculatedWave > session.currentWave) {
            session.currentWave = calculatedWave;
            session.waveTextTimer = 3f; // Show text for 3 seconds
        }

        // 4. Calculate Difficulty Math (No DDA, strictly math-based)
        // Rate gets faster: Wave 1 = 1.0s, Wave 2 = 0.8s ... caps at 0.2s
        float spawnRate = Math.max(0.2f, 1.2f - (session.currentWave * 0.2f));

        // Count gets higher: Wave 1 = 1 enemy, Wave 3 = 2 enemies, Wave 5 = 3 enemies
        int spawnAmount = 1 + (session.currentWave / 2);

        // 5. Spawn Logic
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnRate) {
            playerEntity = ECSUtils.getPlayer(getEngine());
            if (playerEntity != null) {
                TransformComponent pTransform = tm.get(playerEntity);

                // THE JUICE: Instead of pure random, we pick a base angle...
                float baseAngle = MathUtils.random(0f, 360f);
                float radius = 600f; // Just outside the camera view

                // ...and spawn an ARC of enemies!
                for (int i = 0; i < spawnAmount; i++) {
                    // Offset each enemy by 15 degrees to form a wall
                    float angle = baseAngle + (i * 15f);

                    float x = pTransform.x + MathUtils.cosDeg(angle) * radius;
                    float y = pTransform.y + MathUtils.sinDeg(angle) * radius;

                    entityFactory.createEnemy(x, y);
                }
            }
            spawnTimer -= spawnRate;
        }
    }
}
