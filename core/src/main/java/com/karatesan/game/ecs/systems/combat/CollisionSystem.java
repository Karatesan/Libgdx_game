package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.HitSourceType;
import com.karatesan.game.ecs.components.combat.projectile.BulletComponent;
import com.karatesan.game.ecs.components.combat.projectile.ExplosionComponent;
import com.karatesan.game.ecs.components.combat.projectile.ProjectileHitHistoryComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.projectile.ProjectileDistanceTravelledComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class CollisionSystem extends EntitySystem implements PausableSystem {

    // We define the two groups we want to compare
    private final Family bulletFamily = Family.all(BulletComponent.class, TransformComponent.class,
        HitboxComponent.class, DamagePayloadComponent.class).get();
    private final Family enemyFamily = Family.all(EnemyComponent.class, TransformComponent.class, HitboxComponent.class,
        HealthComponent.class).get();
    private final Family explosionFamily = Family.all(ExplosionComponent.class, TransformComponent.class,
        HitboxComponent.class, DamagePayloadComponent.class).exclude(PendingRemovalComponent.class).get();

    private final GameContext context;

    private ImmutableArray<Entity> bullets;
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> explosions;


    public CollisionSystem(GameContext context) {
        this.context = context;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        bullets = engine.getEntitiesFor(bulletFamily);
        enemies = engine.getEntitiesFor(enemyFamily);
        explosions = engine.getEntitiesFor(explosionFamily);
    }

    @Override
    public void update(float deltaTime) {
        checkPlayerEnemyCollisions();
        checkBulletEnemyCollisions();
        checkExplosionEnemyCollisions();
    }

    private void checkBulletEnemyCollisions() {
        for (int i = 0; i < bullets.size(); ++i) {
            Entity bullet = bullets.get(i);

            if (Mappers.pendingRemoval.has(bullet) || Mappers.dead.has(bullet)) {
                continue;
            }

            TransformComponent bPos = Mappers.transform.get(bullet);
            HitboxComponent bBox = Mappers.hitbox.get(bullet);
            ProjectileHitHistoryComponent history = Mappers.hitHistory.get(bullet);

            for (int j = 0; j < enemies.size(); ++j) {
                Entity enemy = enemies.get(j);

                if (Mappers.dead.has(enemy)) {
                    continue;
                }

                TransformComponent ePos = Mappers.transform.get(enemy);
                HitboxComponent eBox = Mappers.hitbox.get(enemy);

                if (!checkCollision(bPos, ePos, bBox.radius, eBox.radius)) {
                    continue;
                }

                if (history.hitTargets.contains(enemy)) {
                    continue;
                }

                history.hitTargets.add(enemy);
                createProjectileHit(bullet, enemy);

                break;
            }
        }
    }

    private void checkExplosionEnemyCollisions() {
        for (int i = 0; i < explosions.size(); ++i) {
            Entity explosion = explosions.get(i);

            ExplosionComponent explosionComponent = Mappers.explosionTag.get(explosion);
            if (explosionComponent.damageApplied) {
                continue;
            }

            TransformComponent exPos = Mappers.transform.get(explosion);
            HitboxComponent exBox = Mappers.hitbox.get(explosion);

            for (int j = 0; j < enemies.size(); ++j) {
                Entity enemy = enemies.get(j);

                if (Mappers.dead.has(enemy)) {
                    continue;
                }

                TransformComponent ePos = Mappers.transform.get(enemy);
                HitboxComponent eBox = Mappers.hitbox.get(enemy);

                if (checkCollision(exPos, ePos, exBox.radius, eBox.radius)) {
                    createExplosionHit(explosion, enemy);
                }
            }

            explosionComponent.damageApplied = true;
        }
    }

    //TODO change it to pure hitbox vs hitbox checks so it does not know about player?
    private void checkPlayerEnemyCollisions() {
        Entity player = context.getPlayer();

        if (player == null) return;
        if (Mappers.invincibility.has(player)) return; // exit entirely, no loop needed

        TransformComponent pPos = Mappers.transform.get(player);
        HitboxComponent pBox = Mappers.hitbox.get(player);

        for (int i = 0; i < enemies.size(); i++) {
            Entity enemy = enemies.get(i);
            if (Mappers.dead.has(enemy)) continue;
            TransformComponent ePos = Mappers.transform.get(enemy);
            HitboxComponent eBox = Mappers.hitbox.get(enemy);
            if (checkCollision(pPos, ePos, pBox.radius, eBox.radius)) {
                createEnemyContactHit(enemy, player);
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

    private void createProjectileHit(Entity bullet, Entity enemy) {
        DamagePayloadComponent payload = Mappers.damage.get(bullet);

        Entity eventEntity = getEngine().createEntity();

        HitEventComponent hit = getEngine().createComponent(HitEventComponent.class);
        hit.target = enemy;
        hit.source = bullet;
        hit.attacker = payload.owner;
        hit.sourceType = HitSourceType.PROJECTILE;

        hit.rawDamage = payload.currentDamage;
        hit.finalDamage = payload.currentDamage;

        TransformComponent enemyTransform = Mappers.transform.get(enemy);

        hit.x = enemyTransform.x;
        hit.y = enemyTransform.y;

        eventEntity.add(hit);
        getEngine().addEntity(eventEntity);
    }

    private void createExplosionHit(Entity explosion, Entity enemy) {
        DamagePayloadComponent payload = Mappers.damage.get(explosion);

        Entity eventEntity = getEngine().createEntity();

        HitEventComponent hit = getEngine().createComponent(HitEventComponent.class);
        hit.target = enemy;
        hit.source = explosion;
        hit.attacker = payload.owner;
        hit.sourceType = HitSourceType.EXPLOSION;
        hit.rawDamage = payload.currentDamage;
        hit.finalDamage = payload.currentDamage;

        TransformComponent enemyTransform = Mappers.transform.get(enemy);
        hit.x = enemyTransform.x;
        hit.y = enemyTransform.y;

        eventEntity.add(hit);
        getEngine().addEntity(eventEntity);
    }

    private void createEnemyContactHit(Entity enemy, Entity player) {
        ContactDamageComponent contact = Mappers.contactDamage.get(enemy);

        Entity eventEntity = getEngine().createEntity();

        HitEventComponent hit = getEngine().createComponent(HitEventComponent.class);
        hit.target = player;
        hit.source = enemy;
        hit.attacker = enemy;
        hit.sourceType = HitSourceType.ENEMY_CONTACT;

        hit.rawDamage = contact.damage;
        hit.finalDamage = contact.damage;

        TransformComponent enemyTransform = Mappers.transform.get(enemy);

        hit.x = enemyTransform.x;
        hit.y = enemyTransform.y;

        eventEntity.add(hit);
        getEngine().addEntity(eventEntity);
    }
}
