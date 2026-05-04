package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.economy.PullableComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.stats.UtilityStatsComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class MagnetSystem extends IteratingSystem implements PausableSystem {

    private final GameContext context;
    private final GameConfig config;

    public MagnetSystem(GameContext context, GameConfig config) {
        // Look for ANYTHING that is Pullable and has a Transform/Velocity
        super(Family.all(PullableComponent.class, TransformComponent.class, VelocityComponent.class).get());
        this.context = context;
        this.config = config;
    }

    @Override
    protected void processEntity(Entity xpEntity, float deltaTime) {
        Entity player = context.getPlayer();
        if (player == null) return;

        TransformComponent pPos = Mappers.transform.get(player);
        UtilityStatsComponent stats = Mappers.utility.get(player);

        TransformComponent xpPos = Mappers.transform.get(xpEntity);
        VelocityComponent xpVel = Mappers.velocity.get(xpEntity);

        // Calculate distance squared (Zero-GC, highly performant)
        float dx = pPos.x - xpPos.x;
        float dy = pPos.y - xpPos.y;
        float distSq = (dx * dx) + (dy * dy);
        float magnetRadiusSq = stats.pickupRadius * stats.pickupRadius;

        // If the XP is inside the magnetic field...
        if (distSq <= magnetRadiusSq) {
            float distance = (float) Math.sqrt(distSq);

            // Prevent divide by zero if they are exactly on top of each other
            if (distance > 0.1f) {
                // Normalize the direction vector
                float nx = dx / distance;
                float ny = dy / distance;

                float pullSpeed = config.pullSpeed;

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
