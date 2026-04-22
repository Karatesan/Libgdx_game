package com.karatesan.game.ecs.systems.combat.perks;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.combat.BulletDataComponent;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class RicochetPerkSystem extends IteratingSystem implements PausableSystem {

    // Mappers for the Bullet
    private final ComponentMapper<HitEventComponent> hm = ComponentMapper.getFor(HitEventComponent.class);
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<ShapeComponent> sm = ComponentMapper.getFor(ShapeComponent.class);
    private final ComponentMapper<BulletDataComponent> bm = ComponentMapper.getFor(BulletDataComponent.class);
    private final ComponentMapper<DamagePayloadComponent> dm = ComponentMapper.getFor(DamagePayloadComponent.class);



    // Cached references (Zero-GC lookups)
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> playerEntity;

    // Static vector to prevent GC allocation during math operations
    private static final Vector2 TEMP_VECTOR = new Vector2();
    private static final float MAX_BOUNCE_RANGE = 400f;
    private static final float MAX_BOUNCE_RANGE_SQ = MAX_BOUNCE_RANGE * MAX_BOUNCE_RANGE;

    public RicochetPerkSystem() {
        // We ONLY iterate over bullets that have just hit something
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);

        // 1. Cache the Player Entity (Assuming single-player, there is only one)
        // We do this here so we don't have to search for the player every time a bullet hits.
        playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());

        // 2. Cache the Live Array of Enemies
        // Ashley automatically keeps this array updated. We just hold the reference.
        // We exclude DeadComponent so bullets don't bounce toward corpses.
        enemies = engine.getEntitiesFor(
            Family.all(EnemyComponent.class, TransformComponent.class).exclude(DeadComponent.class).get());
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        // 1. Check if the player even has the perk
        Entity player = playerEntity.first();
        BulletDataComponent bulletStamp = bm.get(player);

        if (bulletStamp.ricochetChance == 0) return;
        // 2. The Jackpot Roll (Pure mathematical randomness)
        if (MathUtils.random() <= bulletStamp.ricochetChance) {

            HitEventComponent hitEvent = hm.get(eventEntity);
            TransformComponent bulletTx = tm.get(hitEvent.bullet);
            VelocityComponent bulletVel = vm.get(hitEvent.bullet);

            // 3. Find the nearest enemy, ignoring the one we just hitEvent
            Entity newTarget = findAnyEnemyInRange(bulletTx.x, bulletTx.y, hitEvent.targetEntity);

            if (newTarget != null) {
                TransformComponent targetTx = tm.get(newTarget);
                // 4. Calculate current speed (Pythagorean theorem)
                // We only do this math IF a ricochet actually happens, saving CPU cycles.

                // 5. Get direction vector to new target and normalize it
                TEMP_VECTOR.set(targetTx.x - bulletTx.x, targetTx.y - bulletTx.y).nor();
                bulletTx.rotation = TEMP_VECTOR.angleDeg();

                ShapeComponent bulletShapeComponent = sm.get(hitEvent.bullet);
                bulletShapeComponent.color = Color.BLUE;

                BulletComponent bulletData = hitEvent.bullet.getComponent(BulletComponent.class);
                bulletData.distanceTravelled = 0;
                bulletData.startX = bulletTx.x;
                bulletData.startY = bulletTx.y;
                // 6. Apply the speed to the new normalized direction
                bulletVel.x = TEMP_VECTOR.x * bulletVel.speed;
                bulletVel.y = TEMP_VECTOR.y * bulletVel.speed;
                DamagePayloadComponent payload = dm.get(hitEvent.bullet);
                //payload.reset();

                // 7. Grant the Pardon!
                // This tells the BulletLifecycleSystem (which runs later) NOT to kill this bullet.
                hitEvent.bullet.add(getEngine().createComponent(PardonedComponent.class));
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

            if (enemy == excludeEntity) continue;

            TransformComponent enemyTx = tm.get(enemy);

            float dx = enemyTx.x - startX;
            float dy = enemyTx.y - startY;
            float distSq = (dx * dx) + (dy * dy);

            // THE OPTIMIZATION: First one inside the radius wins!
            if (distSq <= MAX_BOUNCE_RANGE_SQ) {
                return enemy; // Instantly exit the loop!
            }
        }

        return null; // No enemies within bounce range
    }
}
