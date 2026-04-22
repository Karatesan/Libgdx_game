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
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.core.PausableSystem;
import com.karatesan.game.ecs.utility.ECSUtils;

public class CollisionSystem extends EntitySystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<HitboxComponent> hm = ComponentMapper.getFor(HitboxComponent.class);
    private final ComponentMapper<HealthComponent> healthM = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<DamagePayloadComponent> dmgM = ComponentMapper.getFor(DamagePayloadComponent.class);
    private final ComponentMapper<DeadComponent> deadM = ComponentMapper.getFor(DeadComponent.class);
    private final ComponentMapper<InvincibilityComponent> im = ComponentMapper.getFor(InvincibilityComponent.class);
    // We define the two groups we want to compare
    private final Family bulletFamily = Family.all(BulletComponent.class, TransformComponent.class,
        HitboxComponent.class, DamagePayloadComponent.class).get();
    private final Family enemyFamily = Family.all(EnemyComponent.class, TransformComponent.class, HitboxComponent.class,
        HealthComponent.class).get();

    private ImmutableArray<Entity> playerEntities;

    private ImmutableArray<Entity> bullets;
    private ImmutableArray<Entity> enemies;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        bullets = engine.getEntitiesFor(bulletFamily);
        enemies = engine.getEntitiesFor(enemyFamily);
        playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        checkPlayerEnemyCollisions();
        checkBulletEnemyCollisions();
    }

    private void checkBulletEnemyCollisions() {
        // Double loop: Check every bullet against every enemy
        for (int i = 0; i < bullets.size(); ++i) {
            Entity bullet = bullets.get(i);

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
                    Entity event = getEngine().createEntity();
                    // 2. Mark Bullet as Hit
                    HitEventComponent hitEvent = getEngine().createComponent(HitEventComponent.class);
                    hitEvent.targetEntity = enemy;
                    hitEvent.bullet = bullet;
                    hitEvent.damage = payload.damage;
                    hitEvent.isCrit = payload.isCrit;
                    event.add(hitEvent);
                    getEngine().addEntity(event);

                    // Break out of the enemy loop (this bullet can't hit a second enemy)
                    break;
                }
            }
        }
    }

    private void checkPlayerEnemyCollisions() {
        if (playerEntities.size() == 0) return;
        Entity playerEntity = playerEntities.first();
        if (im.has(playerEntity)) return; // exit entirely, no loop needed

        TransformComponent pPos = tm.get(playerEntity);
        HitboxComponent pBox = hm.get(playerEntity);
        HealthComponent pH = healthM.get(playerEntity);

        for (int i = 0; i < enemies.size(); i++) {
            Entity enemy = enemies.get(i);
            if (deadM.has(enemy)) continue;
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


    private boolean checkCollision(TransformComponent bPos, TransformComponent ePos, float bRadius, float eRadius) {
        float dx = bPos.x - ePos.x;
        float dy = bPos.y - ePos.y;
        float distanceSquared = (dx * dx) + (dy * dy);

        float combinedRadii = bRadius + eRadius;
        float radiiSquared = combinedRadii * combinedRadii;
        return distanceSquared <= radiiSquared;
    }
}
