package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.combat.InvincibilityComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.ECSUtils;

public class CollisionSystem extends EntitySystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<HitboxComponent> hm = ComponentMapper.getFor(HitboxComponent.class);
    private final ComponentMapper<HealthComponent> healthM = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<DamagePayloadComponent> dmgM = ComponentMapper.getFor(DamagePayloadComponent.class);
    private final ComponentMapper<DeadComponent> deadM = ComponentMapper.getFor(DeadComponent.class);
    private final ComponentMapper<InvincibilityComponent> im = ComponentMapper.getFor(InvincibilityComponent.class);
    private final ComponentMapper<HitEventComponent> hem = ComponentMapper.getFor(HitEventComponent.class);
    // We define the two groups we want to compare
    private final Family bulletFamily = Family.all(BulletComponent.class, TransformComponent.class,
        HitboxComponent.class, DamagePayloadComponent.class).get();
    private final Family enemyFamily = Family.all(EnemyComponent.class, TransformComponent.class, HitboxComponent.class,
        HealthComponent.class).get();

    private Entity playerEntity;
    private EntityFactory entityFactory;

    public CollisionSystem(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    public void update(float deltaTime) {
        PooledEngine engine = (PooledEngine) getEngine();

        ImmutableArray<Entity> bullets = engine.getEntitiesFor(bulletFamily);
        ImmutableArray<Entity> enemies = engine.getEntitiesFor(enemyFamily);

        playerEntity = ECSUtils.getPlayer(getEngine());
        checkPlayerEnemyCollisions(enemies);
        checkBulletEnemyCollisions(bullets, enemies, engine);
    }

    private void checkBulletEnemyCollisions(ImmutableArray<Entity> bullets, ImmutableArray<Entity> enemies,
                                            PooledEngine engine) {
        // Double loop: Check every bullet against every enemy
        for (int i = 0; i < bullets.size(); ++i) {
            Entity bullet = bullets.get(i);

            // If bullet already hit (hit something this frame), skip it
            if (hem.has(bullet)) continue;

            TransformComponent bPos = tm.get(bullet);
            HitboxComponent bBox = hm.get(bullet);
            DamagePayloadComponent payload = dmgM.get(bullet);

            for (int j = 0; j < enemies.size(); ++j) {
                Entity enemy = enemies.get(j);

                // If enemy is already dead, skip it
                if (deadM.has(enemy)) continue;

                TransformComponent ePos = tm.get(enemy);
                HitboxComponent eBox = hm.get(enemy);

                // If they hit!
                if (checkCollision(bPos, ePos, bBox.radius, eBox.radius)) {

                    // 1. Deal Damage
                    HealthComponent eHealth = healthM.get(enemy);
                    eHealth.currentHp -= payload.damage;

                    // 2. Mark Bullet as Hit
                    HitEventComponent hitEvent = engine.createComponent(HitEventComponent.class);
                    hitEvent.targetEntity = enemy;
                    bullet.add(hitEvent);

                    //Create floating text
                    entityFactory.createDamageText(enemy, payload.damage, payload.isCrit);

                    // 3. Check if Enemy died
                    if (eHealth.currentHp <= 0) {
                        enemy.add(engine.createComponent(DeathEventComponent.class));
                    }
                    // Break out of the enemy loop (this bullet can't hit a second enemy)
                    break;
                }
            }
        }
    }

    private void checkPlayerEnemyCollisions(ImmutableArray<Entity> enemies) {
        if (playerEntity != null) {
            TransformComponent pPos = tm.get(playerEntity);
            HitboxComponent pBox = hm.get(playerEntity);
            HealthComponent pH = healthM.get(playerEntity);

            for (Entity enemy : enemies) {
                if (deadM.has(enemy) || im.has(playerEntity)) continue;
                TransformComponent ePos = tm.get(enemy);
                HitboxComponent eBox = hm.get(enemy);
                if (checkCollision(pPos, ePos, pBox.radius, eBox.radius)) {
                    pH.currentHp -= 10f;
                    if (pH.currentHp <= 0) {
                        playerEntity.add(getEngine().createComponent(FatalDamageComponent.class));
                    } else {
                        playerEntity.add(getEngine().createComponent(InvincibilityComponent.class));
                    }
                    break;
                }
            }
        }
    }

    private boolean checkCollision(TransformComponent bPos, TransformComponent ePos, float bRadius, float eRadius) {
        float dx = bPos.x - ePos.x;
        float dy = bPos.y - ePos.y;
        float distanceSquared = (dx * dx) + (dy * dy);

        float combinedRadii = bRadius + eRadius;
        float radiiSquared = combinedRadii * combinedRadii;
        return distanceSquared <= radiiSquared;
    }
}
