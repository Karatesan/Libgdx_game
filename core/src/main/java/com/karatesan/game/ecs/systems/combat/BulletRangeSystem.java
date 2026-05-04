package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.weapon.ProjectileDistanceTravelledComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.utility.PausableSystem;

//Handles bullet range expiration
public class BulletRangeSystem extends IteratingSystem implements PausableSystem {

    public BulletRangeSystem() {
        // This system only cares about entities that have a BulletComponent
        super(Family.all(BulletComponent.class, ProjectileDistanceTravelledComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BulletComponent bullet = Mappers.bullet.get(entity);
        VelocityComponent velocity = Mappers.velocity.get(entity);
        ProjectileDistanceTravelledComponent distanceTravelled = Mappers.distanceTravelled.get(entity);

        float distance = Vector2.len(velocity.x, velocity.y) * deltaTime;
        distanceTravelled.distanceTravelled += distance;

        if (distanceTravelled.distanceTravelled >= bullet.range) {
            getEngine().removeEntity(entity);
        }
    }
}
