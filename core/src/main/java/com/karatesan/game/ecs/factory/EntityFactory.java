package com.karatesan.game.ecs.factory;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.render.FloatingTextComponent;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.ecs.components.core.LifeTimeComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.economy.CollectibleComponent;
import com.karatesan.game.ecs.components.economy.PullableComponent;
import com.karatesan.game.ecs.components.economy.XpComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public class EntityFactory {
    private static final float[] OFFSET_X = {-13f, 0f, 13f, -19f, 19f, -10f, 10f, 0f};
    private static final float[] OFFSET_Y = {15f, 20f, 15f, 5f, 5f, 30f, 30f, 5f};
    private static final Color[] colors = {Color.RED, Color.BLUE, Color.FIREBRICK, Color.DARK_GRAY, Color.FOREST};
    private int textOffsetIndex = 0;

    private final PooledEngine engine;

    public EntityFactory(PooledEngine engine) {
        this.engine = engine;
    }

    public Entity createEntity() {
        return engine.createEntity();
    }

    public void createPlayer() {
        // Ask the engine for a clean, recycled Entity
        Entity player = engine.createEntity();

        // Ask the engine for clean, recycled Components
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        PlayerComponent playerTag = engine.createComponent(PlayerComponent.class);
        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        StatsComponent stats = engine.createComponent(StatsComponent.class);
        MovementComponent movement = engine.createComponent(MovementComponent.class);
        HealthComponent health = engine.createComponent(HealthComponent.class);
        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        PerkInventoryComponent inventory = engine.createComponent(PerkInventoryComponent.class);

        // Set the starting data
        transform.x = 400;
        transform.y = 300;
        transform.z = 1;
        transform.size = 32;
        // Velocity starts at 0, the InputSystem will change it!
        velocity.x = 0;
        velocity.y = 0;
        shape.color = Color.BROWN;

        movement.maxSpeed = 100f;

        health.maxHp = 100f;
        health.currentHp = 100f;

        hitbox.radius = 16f; // Half of size 32

        BulletDataComponent bulletData = engine.createComponent(BulletDataComponent.class);
        bulletData.ricochetChance = 1f;
        // Glue the components to the Entity
        player.add(transform);
        player.add(velocity);
        player.add(playerTag);
        player.add(shape);
        player.add(stats);
        player.add(movement);
        player.add(health);
        player.add(hitbox);
        player.add(inventory);
        player.add(bulletData);
        equipBasicWeapon(player);

        // Add the finished Entity to the Engine
        engine.addEntity(player);
    }

    public void createEnemy(float x, float y, EnemyType type) {
        Entity enemy = engine.createEntity();


        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = x;
        transform.y = y;

        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);

        // 2. Variable Components (These change based on the type!)
        HealthComponent health = engine.createComponent(HealthComponent.class);
        MovementComponent movement = engine.createComponent(MovementComponent.class);
        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        ShapeComponent shape = engine.createComponent(ShapeComponent.class);

        // 1. Base Components (Every enemy gets these)
        EnemyComponent enemyComp = engine.createComponent(EnemyComponent.class);

        // 3. The Data-Driven Configuration
        switch (type) {
            case STANDARD:
                health.maxHp = 20f;
                movement.maxSpeed = 100f;
                hitbox.radius = 15f;
                transform.size = 30f; // Visual size (Diameter)
                shape.color = Color.WHITE;
                enemyComp.xpDropValue = 3f;
                break;

            case SWARMER:
                health.maxHp = 5f; // Dies in 1 hit usually
                movement.maxSpeed = 220f; // Extremely fast!
                hitbox.radius = 8f; // Harder to hit
                transform.size = 16f;
                shape.color = Color.RED; // Red = Danger/Fast
                enemyComp.xpDropValue = 1f;
                break;

            case TANK:
                health.maxHp = 150f; // Bullet sponge
                movement.maxSpeed = 40f; // Very slow, creeping doom
                hitbox.radius = 35f; // Massive body blocks bullets
                transform.size = 70f;
                shape.color = Color.ROYAL; // Purple/Blue = Heavy
                enemyComp.xpDropValue = 15f;
                break;
        }

        health.currentHp = health.maxHp;

        enemy.add(transform).add(velocity).add(health).add(movement).add(hitbox).add(shape).add(enemyComp);

        engine.addEntity(enemy);
    }


    // --- WEAPON CONFIGURATORS ---
    public void equipBasicWeapon(Entity player) {
        player.remove(WeaponComponent.class);
        WeaponComponent weapon = engine.createComponent(WeaponComponent.class);
        weapon.minDamage = 3f;
        weapon.maxDamage = 5f;
        weapon.fireRate = 0.8f;
        weapon.projectileCount = 1;
        weapon.spreadAngle = 10f; // Slight inaccuracy
        weapon.projectileSpeed = 1000f;
        weapon.range = 500f;

        player.add(weapon);
    }

    // --- BULLET SPAWNER ---
    // Your WeaponSystem will call this method!
    public void createBullet(TransformComponent playerTransform, WeaponComponent weapon, BulletDataComponent bulletData,
                             StatsComponent stats, float angle) {
        Entity bullet = engine.createEntity();

        boolean isCrit = MathUtils.random() <= stats.critChance;
        float damage = calculateDamage(weapon.minDamage, weapon.maxDamage, stats, isCrit);

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = playerTransform.x;
        transform.y = playerTransform.y;
        transform.z = 2;
        transform.size = 4; //TODO hardcoded bullet size
        transform.rotation = angle;

        float angleRad = angle * MathUtils.degreesToRadians;

        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        velocity.x = MathUtils.cos(angleRad) * weapon.projectileSpeed;
        velocity.y = MathUtils.sin(angleRad) * weapon.projectileSpeed;
        velocity.speed = weapon.projectileSpeed;

        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        shape.color = isCrit ? Color.RED : Color.LIGHT_GRAY; // Make crits look cool!

        BulletComponent bulletTag = engine.createComponent(BulletComponent.class);
        bulletTag.range = weapon.range;
        // ADD THESE: Anchor the trail to the spawn position
        bulletTag.startX = playerTransform.x;
        bulletTag.startY = playerTransform.y;

        //STAMP PERKS -------------------------------------------------------
        bulletTag.pierceCount = bulletData.pierceCount;
        bulletTag.ricochetChance = bulletData.ricochetChance;

        DamagePayloadComponent payload = engine.createComponent(DamagePayloadComponent.class);
        payload.damage = damage;
        payload.isCrit = isCrit;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = 4;

        PierceComponent pierceComponent = engine.createComponent(PierceComponent.class);

        bullet.add(transform);
        bullet.add(velocity);
        bullet.add(shape);
        bullet.add(bulletTag);
        bullet.add(payload);
        bullet.add(hitbox);
        bullet.add(pierceComponent);

        engine.addEntity(bullet);
    }

    private float calculateDamage(float minDamage, float maxDamage, StatsComponent stats, boolean isCrit) {
        float damage = MathUtils.random(minDamage, maxDamage);
        damage *= stats.damageMultiplier;
        if (isCrit) {
            damage *= stats.critMultiplier;
        }
        return damage;
    }


    public void createBullet(float x, float y, float angleRad, float speed, float damage, float range, boolean isCrit) {
        Entity bullet = engine.createEntity();

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = x;
        transform.y = y;
        transform.z = 2;
        transform.size = 4;
        transform.rotation = angleRad * MathUtils.radiansToDegrees;

        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        velocity.x = MathUtils.cos(angleRad) * speed;
        velocity.y = MathUtils.sin(angleRad) * speed;
        velocity.speed = speed;

        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        shape.color = isCrit ? Color.RED : Color.LIGHT_GRAY; // Make crits look cool!

        BulletComponent bulletData = engine.createComponent(BulletComponent.class);
        bulletData.range = range;
        // ADD THESE: Anchor the trail to the spawn position
        bulletData.startX = x;
        bulletData.startY = y;

        DamagePayloadComponent payload = engine.createComponent(DamagePayloadComponent.class);
        payload.damage = damage;
        payload.isCrit = isCrit;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = 4;

        PierceComponent pierceComponent = engine.createComponent(PierceComponent.class);

        bullet.add(transform);
        bullet.add(velocity);
        bullet.add(shape);
        bullet.add(bulletData);
        bullet.add(payload);
        bullet.add(hitbox);
        bullet.add(pierceComponent);

        engine.addEntity(bullet);
    }

    public void createSession() {
        Entity sessionEntity = engine.createEntity();
        SessionComponent sessionData = engine.createComponent(SessionComponent.class);
        sessionEntity.add(sessionData);
        engine.addEntity(sessionEntity);
    }

    public void createDamageText(Entity enemy, float damage, boolean isCrit) {
        Entity text = engine.createEntity();

        // 1. Setup Lifetime
        LifeTimeComponent lifetime = engine.createComponent(LifeTimeComponent.class);
        lifetime.timer = 0.5f;
        lifetime.maxTime = 0.5f;

        // 2. Setup Text Data (Store INT, not String)
        FloatingTextComponent txt = engine.createComponent(FloatingTextComponent.class);
        txt.damageValue = MathUtils.round(damage);
        txt.color = isCrit ? Color.RED : Color.WHITE;

        txt.scale = isCrit ? 1f : .75f;

        // 3. Setup Position (Copy enemy's current position)
        TransformComponent enemyPos = enemy.getComponent(TransformComponent.class);
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = enemyPos.x + OFFSET_X[textOffsetIndex];
        transform.y = enemyPos.y + OFFSET_Y[textOffsetIndex];
        transform.z = 3;

        // 4. Setup "Fountain" Velocity (Jitter)
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        // Multiply the X offset by 1.5.
        // If it spawned at -20 (left), it flies left at -30 speed.
        // If it spawned at +30 (right), it flies right at +45 speed.
        velocity.x = OFFSET_X[textOffsetIndex] * 0.35f;

        // Give it a base upward speed (40f), plus a little extra based on its Y offset.
        // Higher spawning numbers will fly slightly faster so they don't get rear-ended.
        velocity.y = 10f + (OFFSET_Y[textOffsetIndex] * 0.27f);

        textOffsetIndex++;
        if (textOffsetIndex >= OFFSET_X.length) {
            textOffsetIndex = 0;
        }

        text.add(lifetime).add(txt).add(transform).add(velocity);
        engine.addEntity(text);
    }

    public void createXpDrop(float x, float y, float value) {
        Entity xp = engine.createEntity();

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = x;
        transform.y = y;
        // A 1 XP crystal is small (8.5f). A 15 XP crystal from a Tank is large (15.5f)!
        transform.size = 8f + (value * 0.5f);

        XpComponent xpComp = engine.createComponent(XpComponent.class);
        xpComp.value = value;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = transform.size / 2f;

        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        shape.color = com.badlogic.gdx.graphics.Color.CYAN; // Classic glowing XP color

        // We add velocity so the MagnetSystem can pull it later
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);

        xp.add(engine.createComponent(PullableComponent.class));
        xp.add(engine.createComponent(CollectibleComponent.class));
        xp.add(transform).add(xpComp).add(hitbox).add(shape).add(velocity);
        engine.addEntity(xp);
    }
}
