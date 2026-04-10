package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.combat.StatsComponent;
import com.karatesan.game.ecs.components.combat.WeaponComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.factory.EntityFactory;

public class WeaponSystem extends IteratingSystem {

    private final EntityFactory entityFactory;
    private final ComponentMapper<TransformComponent> tc = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<PlayerComponent> pc = ComponentMapper.getFor(PlayerComponent.class);
    private final ComponentMapper<WeaponComponent> wc = ComponentMapper.getFor(WeaponComponent.class);
    private final ComponentMapper<StatsComponent> sc = ComponentMapper.getFor(StatsComponent.class);

    public WeaponSystem(EntityFactory entityFactory) {
        super(Family.all(TransformComponent.class, PlayerComponent.class, WeaponComponent.class,
            StatsComponent.class).get());
        this.entityFactory = entityFactory;
    }


    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tc.get(entity);
        StatsComponent stats = sc.get(entity);
        WeaponComponent weapon = wc.get(entity);
        PlayerComponent player = pc.get(entity);

        weapon.shootTimer += deltaTime;

        if (player.isShooting && weapon.shootTimer >= weapon.fireRate) {

            for (int i = 0; i < weapon.projectileCount; i++) {
                boolean isCrit = MathUtils.random() <= stats.critChance;
                float damage = calculateDamage(weapon.minDamage, weapon.maxDamage, stats, isCrit);

                // 1. Calculate the starting angle (in degrees)
                float baseAngleDeg = transform.rotation;

                float angleOffset = 0;
                if (weapon.projectileCount > 1) {
                    // 'i' is your current loop index
                    float fraction = (float) i / (weapon.projectileCount - 1);
                    angleOffset = (fraction - 0.5f) * weapon.spreadAngle;
                }
                // Apply the offset and convert back to radians for the bullet velocity math
                float finalAngleRad = (baseAngleDeg + angleOffset) * MathUtils.degreesToRadians;
                entityFactory.createBullet(transform.x, transform.y, finalAngleRad, weapon.projectileSpeed, damage, weapon.range,
                    isCrit);
            }
            weapon.shootTimer = 0;
        }
    }

    private float calculateDamage(float minDamage, float maxDamage, StatsComponent stats, boolean isCrit) {
        float damage = MathUtils.random(minDamage, maxDamage);
        damage *= stats.damageMultiplier;
        if (isCrit) {
            damage *= stats.critMultiplier;
        }
        return damage;
    }
}
