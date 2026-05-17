package com.karatesan.game.ecs.logic.combat.projectile;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;

public final class ProjectileMotionHelper {

    private ProjectileMotionHelper() {
    }

    public static boolean redirectToward(Entity bullet, Entity target) {
        TransformComponent bulletTransform = Mappers.transform.get(bullet);
        TransformComponent targetTransform = Mappers.transform.get(target);
        VelocityComponent velocity = Mappers.velocity.get(bullet);

        float dx = targetTransform.x - bulletTransform.x;
        float dy = targetTransform.y - bulletTransform.y;
        float len2 = dx * dx + dy * dy;

        if (len2 <= 0.0001f) {
            return false;
        }

        float invLen = 1f / (float) Math.sqrt(len2);

        float speed = velocity.speed;
        if (speed <= 0f) {
            speed = (float) Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
        }

        velocity.x = dx * invLen * speed;
        velocity.y = dy * invLen * speed;
        velocity.speed = speed;

        bulletTransform.rotation = MathUtils.atan2(velocity.y, velocity.x) * MathUtils.radiansToDegrees;

        return true;
    }
}
