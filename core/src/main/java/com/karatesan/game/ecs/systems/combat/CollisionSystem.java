package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.utility.PausableSystem;

public class CollisionSystem extends EntitySystem implements PausableSystem {

    // We define the two groups we want to compare
    private final Family bulletFamily = Family.all(BulletComponent.class, TransformComponent.class,
        HitboxComponent.class, DamagePayloadComponent.class).get();
    private final Family enemyFamily = Family.all(EnemyComponent.class, TransformComponent.class, HitboxComponent.class,
        HealthComponent.class).get();
    private final GameContext context;
    private final GameConfig config;
    private ImmutableArray<Entity> bullets;
    private ImmutableArray<Entity> enemies;

    public CollisionSystem(GameContext context, GameConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        bullets = engine.getEntitiesFor(bulletFamily);
        enemies = engine.getEntitiesFor(enemyFamily);
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

            TransformComponent bPos = Mappers.transform.get(bullet);
            HitboxComponent bBox = Mappers.hitbox.get(bullet);
            DamagePayloadComponent payload = Mappers.damage.get(bullet);

            for (int j = 0; j < enemies.size(); ++j) {
                Entity enemy = enemies.get(j);

                BulletComponent bulletComponent = Mappers.bullet.get(bullet);

                // If enemy is already dead, skip it
                if (Mappers.dead.has(enemy)) continue;
                // If enemy was already hit before by this bullet skip it
                if (bulletComponent.lastHit == enemy) continue;

                TransformComponent ePos = Mappers.transform.get(enemy);
                HitboxComponent eBox = Mappers.hitbox.get(enemy);
                // If they hit!
                if (checkCollision(bPos, ePos, bBox.radius, eBox.radius)) {
                    Entity event = getEngine().createEntity();
                    // 2. Mark Bullet as Hit
                    HitEventComponent hitEvent = getEngine().createComponent(HitEventComponent.class);
                    hitEvent.targetEntity = enemy;
                    hitEvent.bullet = bullet;
                    hitEvent.rawDamage = payload.damage;
                    hitEvent.isCrit = payload.isCrit;
                    hitEvent.finalDamage = payload.damage;

                    BulletHitEvent bulletHitEvent = getEngine().createComponent(BulletHitEvent.class);
                    event.add(hitEvent).add(bulletHitEvent);
                    getEngine().addEntity(event);

                    //set last hit for bullet
                    bulletComponent.lastHit = enemy;
                    // Break out of the enemy loop (this bullet can't hit a second enemy)
                    break;
                }
            }
        }
    }

    //TODO change it to pure hitbox vs hitbox checks so it does not know about player?
    private void checkPlayerEnemyCollisions() {
        Entity player = context.getPlayer();
        if (player == null) return;
        if (Mappers.invincibility.has(player)) return; // exit entirely, no loop needed
        TransformComponent pPos = Mappers.transform.get(player);
        HitboxComponent pBox = Mappers.hitbox.get(player);
        HealthComponent pH = Mappers.health.get(player);

        for (int i = 0; i < enemies.size(); i++) {
            Entity enemy = enemies.get(i);
            if (Mappers.dead.has(enemy)) continue;
            TransformComponent ePos = Mappers.transform.get(enemy);
            HitboxComponent eBox = Mappers.hitbox.get(enemy);
            if (checkCollision(pPos, ePos, pBox.radius, eBox.radius)) {
                ContactDamageComponent contactDamageComponent = Mappers.contactDamage.get(enemy);


                Entity event = getEngine().createEntity();
                // 2. Mark Bullet as Hit
                HitEventComponent hitEvent = getEngine().createComponent(HitEventComponent.class);
                hitEvent.targetEntity = player;
                hitEvent.bullet = null;
                hitEvent.rawDamage = contactDamageComponent.damage;
                hitEvent.isCrit = false;
                hitEvent.finalDamage = contactDamageComponent.damage;

                InvincibilityComponent inv = getEngine().createComponent(InvincibilityComponent.class);
                inv.timer = config.iFramesDuration;
                player.add(inv);

                event.add(hitEvent);
                getEngine().addEntity(event);
                break;
//                pH.currentHp -= contactDamageComponent.damage;
//                if (pH.currentHp <= 0) {
//                    player.add(getEngine().createComponent(FatalDamageComponent.class));
//                } else {
//                    InvincibilityComponent inv = getEngine().createComponent(InvincibilityComponent.class);
//                    inv.timer = config.iFramesDuration;
//                    player.add(inv);
//                }
//                break;
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
