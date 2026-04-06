package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;

public class BulletSystem extends IteratingSystem {

    private final ComponentMapper<BulletComponent> bm = ComponentMapper.getFor(BulletComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);

    public BulletSystem() {
        // This system only cares about entities that have a BulletComponent
        super(Family.all(BulletComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BulletComponent bullet = bm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        float distance = Vector2.len(velocity.x, velocity.y) * deltaTime;
        bullet.distanceTravelled += distance;

        if (bullet.distanceTravelled >= bullet.range) {
            getEngine().removeEntity(entity);
        }
    }
}
