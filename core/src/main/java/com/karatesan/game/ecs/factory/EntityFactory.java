package com.karatesan.game.ecs.factory;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.*;
import com.karatesan.game.ecs.components.UI.FloatingTextComponent;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public class EntityFactory {
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

        // Set the starting data
        transform.x = 400;
        transform.y = 300;
        transform.size = 32;
        // Velocity starts at 0, the InputSystem will change it!
        velocity.x = 0;
        velocity.y = 0;
        shape.color = Color.BROWN;

        movement.maxSpeed = 100f;

        health.maxHp = 100f;
        health.currentHp = 100f;

        hitbox.radius = 16f; // Half of size 32

        // Glue the components to the Entity
        player.add(transform);
        player.add(velocity);
        player.add(playerTag);
        player.add(shape);
        player.add(stats);
        player.add(movement);
        player.add(health);
        player.add(hitbox);

        equipShotgun(player);

        // Add the finished Entity to the Engine
        engine.addEntity(player);
    }

    public void createEnemy(float x, float y) {
        Entity enemy = engine.createEntity();

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = x;
        transform.y = y;
        transform.size = 32;

        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);

        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        shape.color = com.badlogic.gdx.graphics.Color.RED; // Enemies are red!

        EnemyComponent enemyTag = engine.createComponent(EnemyComponent.class);

        HealthComponent health = engine.createComponent(HealthComponent.class);
        health.maxHp = 100f;
        health.currentHp = 100f;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = 16f; // Half of size 32

        MovementComponent movement = engine.createComponent(MovementComponent.class);
        movement.maxSpeed = 50f;

        enemy.add(transform);
        enemy.add(velocity);
        enemy.add(shape);
        enemy.add(enemyTag);
        enemy.add(health);
        enemy.add(hitbox);
        enemy.add(movement);

        engine.addEntity(enemy);
    }


    // --- WEAPON CONFIGURATORS ---

    public void equipShotgun(Entity player) {
        WeaponComponent weapon = engine.createComponent(WeaponComponent.class);
        weapon.minDamage = 8f;
        weapon.maxDamage = 12f;
        weapon.fireRate = 0.8f;
        weapon.projectileCount = 6;
        weapon.spreadAngle = 35f;
        weapon.projectileSpeed = 500f;
        weapon.range = 2000f;

        player.add(weapon);
    }

    public void equipMachineGun(Entity player) {
        WeaponComponent weapon = engine.createComponent(WeaponComponent.class);
        weapon.minDamage = 15f;
        weapon.maxDamage = 20f;
        weapon.fireRate = 0.1f; // Super fast!
        weapon.projectileCount = 1;
        weapon.spreadAngle = 5f; // Slight inaccuracy
        weapon.projectileSpeed = 800f;
        weapon.range = 1000f;

        player.add(weapon);
    }

    // --- BULLET SPAWNER ---
    // Your WeaponSystem will call this method!

    public void createBullet(float x, float y, float angleRad, float speed, float damage, float range, boolean isCrit) {
        Entity bullet = engine.createEntity();

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = x;
        transform.y = y;
        transform.size = 8;

        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        velocity.x = (float) Math.cos(angleRad) * speed;
        velocity.y = (float) Math.sin(angleRad) * speed;

        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        shape.color = isCrit ? Color.RED : Color.YELLOW; // Make crits look cool!

        BulletComponent bulletData = engine.createComponent(BulletComponent.class);
        bulletData.range = range;

        DamagePayloadComponent payload = engine.createComponent(DamagePayloadComponent.class);
        payload.damage = damage;
        payload.isCrit = isCrit;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = 4;

        bullet.add(transform);
        bullet.add(velocity);
        bullet.add(shape);
        bullet.add(bulletData);
        bullet.add(payload);
        bullet.add(hitbox);

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

        LifeTimeComponent lifetime = engine.createComponent(LifeTimeComponent.class);
        lifetime.timer = .5f;
        lifetime.maxTime = .5f;

        FloatingTextComponent txt = engine.createComponent(FloatingTextComponent.class);

        TransformComponent transform = engine.createComponent(TransformComponent.class);

        txt.text = String.valueOf(MathUtils.round(damage));
        txt.color = isCrit ? Color.RED : Color.WHITE;
        txt.anchorEntity = enemy;
        text.add(lifetime).add(txt).add(transform);

        engine.addEntity(text);
    }
}
