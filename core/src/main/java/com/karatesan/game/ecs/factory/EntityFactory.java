package com.karatesan.game.ecs.factory;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.data.blueprints.PlayerBlueprint;
import com.karatesan.game.data.blueprints.WeaponBlueprint;
import com.karatesan.game.data.registry.BlueprintRegistry;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.weapon.ProjectileDistanceTravelledComponent;
import com.karatesan.game.ecs.components.weapon.WeaponComponent;
import com.karatesan.game.ecs.components.weapon.WeaponStateComponent;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.ecs.components.economy.*;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.perks.PierceComponent;
import com.karatesan.game.ecs.components.perks.RicochetComponent;
import com.karatesan.game.ecs.components.render.FloatingTextComponent;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.ecs.components.core.LifeTimeComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.stats.DefenseStatsComponent;
import com.karatesan.game.ecs.components.stats.OffensiveStatsComponent;
import com.karatesan.game.ecs.components.stats.UtilityStatsComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.combat.ContactDamageComponent;

public class EntityFactory {
    private static final float[] OFFSET_X = {-13f, 0f, 13f, -19f, 19f, -10f, 10f, 0f};
    private static final float[] OFFSET_Y = {15f, 20f, 15f, 5f, 5f, 30f, 30f, 5f};
    private static final Color[] colors = {Color.RED, Color.BLUE, Color.FIREBRICK, Color.DARK_GRAY, Color.FOREST};
    private int textOffsetIndex = 0;

    private final PooledEngine engine;
    private final GameConfig config;
    private final BlueprintRegistry blueprints;


    public EntityFactory(PooledEngine engine, GameConfig config, BlueprintRegistry blueprints) {
        this.engine = engine;
        this.config = config;
        this.blueprints = blueprints;
    }

    public Entity createPlayer(float x, float y) {
        // Ask the engine for a clean, recycled Entity
        Entity player = engine.createEntity();
        PlayerBlueprint pb = blueprints.getPlayer();
        WeaponBlueprint wb = blueprints.getWeapon();

        // Ask the engine for clean, recycled Components
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        PlayerComponent playerTag = engine.createComponent(PlayerComponent.class);
        ShapeComponent shape = engine.createComponent(ShapeComponent.class);
        MovementComponent movement = engine.createComponent(MovementComponent.class);
        HealthComponent health = engine.createComponent(HealthComponent.class);
        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        PerkInventoryComponent inventory = engine.createComponent(PerkInventoryComponent.class);
        WeaponStateComponent weaponState = engine.createComponent(WeaponStateComponent.class);
        OffensiveStatsComponent offensiveStats = engine.createComponent(OffensiveStatsComponent.class);
        DefenseStatsComponent defensiveComponent = engine.createComponent(DefenseStatsComponent.class);
        UtilityStatsComponent utilityComponent = engine.createComponent(UtilityStatsComponent.class);
        // Set the starting data
        transform.x = x;
        transform.y = y;
        transform.z = 1;
        transform.size = pb.size;
        // Velocity starts at 0, the InputSystem will change it!
        velocity.x = 0;
        velocity.y = 0;
        shape.color = Color.BROWN;

        movement.maxSpeed = pb.moveSpeed;

        health.maxHp = pb.maxHp;
        health.currentHp = pb.maxHp;
        health.hpRegen = pb.hpRegen;

        hitbox.radius = pb.size / 2; // Half of size

        offensiveStats.critMultiplier = pb.critMultiplier;
        offensiveStats.critChance = pb.critChance;

        defensiveComponent.dodgeChance = pb.dodgeChance;
        defensiveComponent.armor = pb.armor;

        utilityComponent.luck = pb.luck;
        utilityComponent.pickupRadius = pb.pickupRadius;
        utilityComponent.xpMultiplier = pb.xpMultiplier;

        ProjectileTemplateComponent projectileTemplate = engine.createComponent(ProjectileTemplateComponent.class);
        projectileTemplate.explosionRadius = config.explosionRadius;
        projectileTemplate.explosionDamageRatio = config.explosionDamageRatio;
        projectileTemplate.ricochetDamageRetention = config.ricochetBaseRetention;
        projectileTemplate.pierceDamageRetention = config.pierceDamageRetention;

        LevelDataComponent levelComponent = engine.createComponent(LevelDataComponent.class);
        // Glue the components to the Entity
        player.add(transform);
        player.add(velocity);
        player.add(playerTag);
        player.add(shape);
        player.add(offensiveStats);
        player.add(defensiveComponent);
        player.add(utilityComponent);
        player.add(movement);
        player.add(health);
        player.add(hitbox);
        player.add(inventory);
        player.add(projectileTemplate);
        player.add(weaponState);
        player.add(levelComponent);
        equipBasicWeapon(player, wb);

        // Add the finished Entity to the Engine
        engine.addEntity(player);
        return player;
    }

    public void createEnemy(float x, float y, EnemyType type, float currentHpScale) {
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

        //Defense
        DefenseStatsComponent defense = engine.createComponent(DefenseStatsComponent.class);
        defense.dodgeChance = 0f;
        defense.armor = 0;

        // 1. Base Components (Every enemy gets these)
        EnemyComponent enemyComp = engine.createComponent(EnemyComponent.class);
        LootDropComponent xpDrop = engine.createComponent(LootDropComponent.class);
        ContactDamageComponent contactDamage = engine.createComponent(ContactDamageComponent.class);

        // 3. The Data-Driven Configuration
        switch (type) {
            case STANDARD:
                health.maxHp = 20f * currentHpScale;
                movement.maxSpeed = 100f;
                hitbox.radius = 15f;
                transform.size = 30f; // Visual size (Diameter)
                shape.color = Color.WHITE;
                xpDrop.xpValue = 3;
                contactDamage.damage = 15;
                break;

            case SWARMER:
                health.maxHp = 5f * currentHpScale; // Dies in 1 hit usually
                movement.maxSpeed = 220f; // Extremely fast!
                hitbox.radius = 8f; // Harder to hit
                transform.size = 16f;
                shape.color = Color.RED; // Red = Danger/Fast
                xpDrop.xpValue = 1;
                contactDamage.damage = 8;
                break;

            case TANK:
                health.maxHp = 150f * currentHpScale; // Bullet sponge
                movement.maxSpeed = 40f; // Very slow, creeping doom
                hitbox.radius = 35f; // Massive body blocks bullets
                transform.size = 70f;
                shape.color = Color.ROYAL; // Purple/Blue = Heavy
                xpDrop.xpValue = 15;
                contactDamage.damage = 30;
                break;
        }

        health.currentHp = health.maxHp;

        enemy.add(transform).add(velocity).add(health).add(movement).add(hitbox).add(shape).add(enemyComp).add(
            contactDamage).add(xpDrop).add(defense);

        engine.addEntity(enemy);
    }

    // --- WEAPON CONFIGURATORS ---
    public void equipBasicWeapon(Entity player, WeaponBlueprint wb) {
        player.remove(WeaponComponent.class);
        WeaponComponent weapon = engine.createComponent(WeaponComponent.class);
        weapon.minDamage = wb.minDamage;
        weapon.maxDamage = wb.maxDamage;
        weapon.fireRate = wb.fireRate;
        weapon.projectileCount = wb.projectileCount;
        weapon.spreadAngle = wb.spreadAngle;
        weapon.inaccuracy = wb.inaccuracy;
        weapon.projectileSpeed = wb.projectileSpeed;
        weapon.range = wb.range;

        player.add(weapon);
    }

    // --- BULLET SPAWNER ---
    // Your WeaponSystem will call this method!
    public void createBullet(TransformComponent playerTransform, WeaponComponent weapon,
                             ProjectileTemplateComponent projectileTemplate, OffensiveStatsComponent stats,
                             float angle) {
        Entity bullet = engine.createEntity();

        boolean isCrit = MathUtils.random() <= stats.critChance;
        float damage = calculateDamage(weapon.minDamage, weapon.maxDamage, stats, isCrit);

        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.x = playerTransform.x;
        transform.y = playerTransform.y;
        transform.z = 2;
        transform.size = config.bulletSize;
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

        //Perks

        if (projectileTemplate.ricochetChance > 0) {
            RicochetComponent ricochet = engine.createComponent(RicochetComponent.class);
            ricochet.ricochetCount = projectileTemplate.ricochetCount;
            ricochet.ricochetChance = projectileTemplate.ricochetChance;
            ricochet.ricochetDamageRetention = projectileTemplate.ricochetDamageRetention;
            bullet.add(ricochet);
        }
        if (projectileTemplate.pierceCount > 0) {
            PierceComponent pierce = engine.createComponent(PierceComponent.class);
            pierce.pierceCount = projectileTemplate.pierceCount;
            pierce.pierceDamageRetention = projectileTemplate.pierceDamageRetention;
            bullet.add(pierce);
        }

        ProjectileDistanceTravelledComponent distanceTravelled = engine.createComponent(
            ProjectileDistanceTravelledComponent.class);

        DamagePayloadComponent payload = engine.createComponent(DamagePayloadComponent.class);
        payload.damage = damage;
        payload.isCrit = isCrit;

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.radius = config.bulletSize;

        bullet.add(transform);
        bullet.add(velocity);
        bullet.add(shape);
        bullet.add(bulletTag);
        bullet.add(payload);
        bullet.add(hitbox);
        bullet.add(distanceTravelled);

        engine.addEntity(bullet);
    }

    private float calculateDamage(float minDamage, float maxDamage, OffensiveStatsComponent stats, boolean isCrit) {
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

    public Entity createSession() {
        Entity sessionEntity = engine.createEntity();
        SessionComponent sessionData = engine.createComponent(SessionComponent.class);
        sessionEntity.add(sessionData);
        engine.addEntity(sessionEntity);
        return sessionEntity;
    }

    public void createDamageText(Entity enemy, float damage, FloatingTextStyle floatingTextStyle) {
        Entity text = engine.createEntity();

        // 1. Setup Lifetime
        LifeTimeComponent lifetime = engine.createComponent(LifeTimeComponent.class);

        // 2. Setup Text Data (Store INT, not String)
        FloatingTextComponent txt = engine.createComponent(FloatingTextComponent.class);
        txt.damageValue = MathUtils.round(damage);
        txt.color = floatingTextStyle.color;

        //Pick scale based on damage type
        if (floatingTextStyle == FloatingTextStyle.CRIT) {
            txt.scale = FloatingTextStyle.CRIT.scale;
            lifetime.timer = FloatingTextStyle.CRIT.lifetime;
            lifetime.maxTime = FloatingTextStyle.CRIT.lifetime;
        }
        else if (floatingTextStyle == FloatingTextStyle.ARMORED) {
            txt.scale = FloatingTextStyle.ARMORED.scale;
            lifetime.timer = FloatingTextStyle.ARMORED.lifetime;
            lifetime.maxTime =FloatingTextStyle.ARMORED.lifetime;
        }
        else {
            txt.scale = FloatingTextStyle.DAMAGE.scale;
            lifetime.timer = FloatingTextStyle.DAMAGE.lifetime;
            lifetime.maxTime = FloatingTextStyle.DAMAGE.lifetime;
        }

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

    public void createFloatingText(Entity enemy, String text, FloatingTextStyle type) {
        Entity textEntity = engine.createEntity();

        // 1. Setup Lifetime
        LifeTimeComponent lifetime = engine.createComponent(LifeTimeComponent.class);
        lifetime.timer = 0.5f;
        lifetime.maxTime = 0.5f;

        // 2. Setup Text Data (Store INT, not String)
        FloatingTextComponent floatingTextComponent = engine.createComponent(FloatingTextComponent.class);
        floatingTextComponent.color = type.color;
        floatingTextComponent.text = text;

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

        textEntity.add(lifetime).add(floatingTextComponent).add(transform).add(velocity);
        engine.addEntity(textEntity);
    }

    public void createXpDrop(float x, float y, int value) {
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
