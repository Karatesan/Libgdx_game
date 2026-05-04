package com.karatesan.game.ecs.systems.perks;

import static com.karatesan.game.ecs.Mappers.*;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.combat.BulletHitEvent;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.weapon.ProjectileDistanceTravelledComponent;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.perks.RicochetComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class RicochetPerkSystem extends IteratingSystem implements PausableSystem {
    // Cached references (Zero-GC lookups)
    private ImmutableArray<Entity> enemies;

    // Static vector to prevent GC allocation during math operations
    private static final Vector2 TEMP_VECTOR = new Vector2();
    private float maxBounceRangeSquare;

    public RicochetPerkSystem(GameConfig config) {
        // We ONLY iterate over bullets that have just hit something
        super(Family.all(HitEventComponent.class, BulletHitEvent.class).get());
        maxBounceRangeSquare = config.ricochetMaxBounceRange * config.ricochetMaxBounceRange;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);

        // 2. Cache the Live Array of Enemies
        // Ashley automatically keeps this array updated. We just hold the reference.
        // We exclude DeadComponent so bullets don't bounce toward corpses.
        enemies = engine.getEntitiesFor(
            Family.all(EnemyComponent.class, TransformComponent.class).exclude(DeadComponent.class).get());
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {

        HitEventComponent hitEventComponent = hitEvent.get(eventEntity);
        RicochetComponent ricochetComponent = ricochet.get(hitEventComponent.bullet);

        if (ricochetComponent == null || ricochetComponent.ricochetCount == 0) return;
        // pierce get priority
        //TODO think how to handle pierce + ricochet perks combined. Maybe only one can be picked?
        if (pierceMarker.has(hitEventComponent.bullet)) return;
        if (MathUtils.random() <= ricochetComponent.ricochetChance) {
            DamagePayloadComponent payload = damage.get(hitEventComponent.bullet);
            ricochetComponent.ricochetCount--;
            //Reduce damage for hit event processing and bullet damage for next target hit
            //we need this dubplciation cause eventhit is destroyed each frame
            hitEventComponent.rawDamage *= ricochetComponent.ricochetDamageRetention;
            payload.damage+= ricochetComponent.ricochetDamageRetention;;

            TransformComponent bulletTx = transform.get(hitEventComponent.bullet);
            VelocityComponent bulletVel = velocity.get(hitEventComponent.bullet);
            BulletComponent bulletComponent = bullet.get(hitEventComponent.bullet);

            // 3. Find the nearest enemy, ignoring the one we just hitEvent
            Entity newTarget = findAnyEnemyInRange(bulletTx.x, bulletTx.y, bulletComponent.lastHit);

            if (newTarget != null) {
                TransformComponent targetTx = transform.get(newTarget);
                // 4. Calculate current speed (Pythagorean theorem)
                // We only do this math IF a ricochet actually happens, saving CPU cycles.

                // 5. Get direction vector to new target and normalize it
                TEMP_VECTOR.set(targetTx.x - bulletTx.x, targetTx.y - bulletTx.y).nor();
                bulletTx.rotation = TEMP_VECTOR.angleDeg();

                ShapeComponent bulletShapeComponent = shape.get(hitEventComponent.bullet);
                bulletShapeComponent.color = Color.BLUE;

                ProjectileDistanceTravelledComponent projectileDistance = distanceTravelled.get(
                    hitEventComponent.bullet);
                projectileDistance.distanceTravelled = 0;

                bulletComponent.startX = bulletTx.x;
                bulletComponent.startY = bulletTx.y;

                // 6. Apply the speed to the new normalized direction
                bulletVel.x = TEMP_VECTOR.x * bulletVel.speed;
                bulletVel.y = TEMP_VECTOR.y * bulletVel.speed;

                // 7. Grant the Pardon!
                // This tells the BulletLifecycleSystem (which runs later) NOT to kill this bullet.
                hitEventComponent.bullet.add(getEngine().createComponent(PardonedComponent.class));
            }
        }
    }

    /**
     * Finds the nearest enemy, excluding the one we just hit.
     * Uses dst2 (distance squared) to avoid expensive Math.sqrt() calls during the loop.
     */
    /**
     * Highly optimized search.
     * Returns the FIRST enemy found within range, drastically reducing CPU cycles.
     */
    private Entity findAnyEnemyInRange(float startX, float startY, Entity excludeEntity) {

        for (int i = 0; i < enemies.size(); ++i) {
            Entity enemy = enemies.get(i);

            if (enemy == excludeEntity) {
                continue;
            }

            TransformComponent enemyTx = transform.get(enemy);

            float dx = enemyTx.x - startX;
            float dy = enemyTx.y - startY;
            float distSq = (dx * dx) + (dy * dy);

            // THE OPTIMIZATION: First one inside the radius wins!
            if (distSq <= maxBounceRangeSquare) {
                return enemy; // Instantly exit the loop!
            }
        }
        return null; // No enemies within bounce range
    }
}
