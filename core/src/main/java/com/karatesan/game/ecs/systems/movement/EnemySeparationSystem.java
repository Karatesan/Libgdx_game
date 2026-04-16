package com.karatesan.game.ecs.systems.movement;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class EnemySeparationSystem extends EntitySystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<HitboxComponent> hm = ComponentMapper.getFor(HitboxComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);

    private ImmutableArray<Entity> enemies;

    @Override
    public void addedToEngine(Engine engine) {
        // Grab all enemies that have physical bodies and can move
        enemies = engine.getEntitiesFor(Family.all(
            EnemyComponent.class, TransformComponent.class,
            HitboxComponent.class, VelocityComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        // The Push Force Multiplier. Tune this!
        // Higher = they bounce away harder. Lower = they squish together more like a fluid.
        float separationForce = 150f;

        // Optimized Double Loop: N * (N - 1) / 2
        for (int i = 0; i < enemies.size(); i++) {
            Entity e1 = enemies.get(i);
            TransformComponent t1 = tm.get(e1);
            HitboxComponent h1 = hm.get(e1);
            VelocityComponent v1 = vm.get(e1);

            for (int j = i + 1; j < enemies.size(); j++) {
                Entity e2 = enemies.get(j);
                TransformComponent t2 = tm.get(e2);
                HitboxComponent h2 = hm.get(e2);

                float dx = t1.x - t2.x;
                float dy = t1.y - t2.y;
                float distSq = (dx * dx) + (dy * dy);
                float combinedRadii = h1.radius + h2.radius;

                // If they are overlapping (and not the exact same position to avoid divide-by-zero)
                if (distSq > 0.001f && distSq < (combinedRadii * combinedRadii)) {

                    // We must use sqrt here to get the actual distance for the push vector
                    float distance = (float) Math.sqrt(distSq);

                    // How deep is the overlap? (0 = barely touching, 20 = completely inside each other)
                    float overlap = combinedRadii - distance;

                    // Normalize the vector (make its length 1)
                    float nx = dx / distance;
                    float ny = dy / distance;

                    // Calculate the push force based on how deep the overlap is
                    float pushX = nx * overlap * separationForce * deltaTime;
                    float pushY = ny * overlap * separationForce * deltaTime;

                    // Push Enemy 1 AWAY from Enemy 2
                    v1.x += pushX;
                    v1.y += pushY;

                    // Push Enemy 2 AWAY from Enemy 1 (Opposite direction)
                    VelocityComponent v2 = vm.get(e2);
                    v2.x -= pushX;
                    v2.y -= pushY;
                }
            }
        }
    }
}
