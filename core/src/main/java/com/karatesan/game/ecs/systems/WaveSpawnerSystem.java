package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.ECSUtils;

public class WaveSpawnerSystem extends EntitySystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private float spawnTimer = 0f;
    private float spawnRate = 1;
    private Entity playerEntity;


    private final EntityFactory entityFactory;

    public WaveSpawnerSystem(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;

        if (spawnTimer >= spawnRate) {
            playerEntity = ECSUtils.getPlayer(getEngine());
            if (playerEntity != null) {
                TransformComponent transformComponent = tm.get(playerEntity);
                float x, y;
                float angle = MathUtils.random(0, 360);
                float radius = 600f;
                x = transformComponent.x + MathUtils.cosDeg(angle) * radius;
                y = transformComponent.y + MathUtils.sinDeg(angle) * radius;
                entityFactory.createEnemy(x, y);
            }
            spawnTimer -= spawnRate;
        }
    }
}
