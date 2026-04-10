package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.economy.PullableComponent;
import com.karatesan.game.ecs.components.economy.MagnetComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.utility.ECSUtils;

public class MagnetSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<MagnetComponent> mm = ComponentMapper.getFor(MagnetComponent.class);

    private Entity playerEntity;

    public MagnetSystem() {
        // Look for ANYTHING that is Pullable and has a Transform/Velocity
        super(Family.all(PullableComponent.class, TransformComponent.class, VelocityComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        playerEntity = ECSUtils.getPlayer(getEngine());
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity xpEntity, float deltaTime) {
        if (playerEntity == null || !mm.has(playerEntity)) return;

        TransformComponent pPos = tm.get(playerEntity);
        MagnetComponent magnet = mm.get(playerEntity);

        TransformComponent xpPos = tm.get(xpEntity);
        VelocityComponent xpVel = vm.get(xpEntity);

        // Calculate distance squared (Zero-GC, highly performant)
        float dx = pPos.x - xpPos.x;
        float dy = pPos.y - xpPos.y;
        float distSq = (dx * dx) + (dy * dy);
        float magnetRadiusSq = magnet.radius * magnet.radius;

        // If the XP is inside the magnetic field...
        if (distSq <= magnetRadiusSq) {
            float distance = (float) Math.sqrt(distSq);

            // Prevent divide by zero if they are exactly on top of each other
            if (distance > 0.1f) {
                // Normalize the direction vector
                float nx = dx / distance;
                float ny = dy / distance;

                // The Pull Speed! 400f feels like a snappy, satisfying vacuum effect.
                float pullSpeed = 220f;

                xpVel.x = nx * pullSpeed;
                xpVel.y = ny * pullSpeed;
            }
        } else {
            // If it's outside the radius, it stops moving (friction)
            xpVel.x = 0f;
            xpVel.y = 0f;
        }
    }
}
