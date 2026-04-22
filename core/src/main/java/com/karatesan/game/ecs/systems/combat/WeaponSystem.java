package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.combat.BulletDataComponent;
import com.karatesan.game.ecs.components.combat.StatsComponent;
import com.karatesan.game.ecs.components.combat.WeaponComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class WeaponSystem extends IteratingSystem implements PausableSystem {

    private final EntityFactory entityFactory;
    private final ComponentMapper<TransformComponent> tc = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<PlayerComponent> pc = ComponentMapper.getFor(PlayerComponent.class);
    private final ComponentMapper<WeaponComponent> wc = ComponentMapper.getFor(WeaponComponent.class);
    private final ComponentMapper<StatsComponent> sc = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<BulletDataComponent> bm = ComponentMapper.getFor(BulletDataComponent.class);

    public WeaponSystem(EntityFactory entityFactory) {
        super(Family.all(TransformComponent.class, PlayerComponent.class, WeaponComponent.class, StatsComponent.class,
            BulletDataComponent.class).get());
        this.entityFactory = entityFactory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tc.get(entity);
        StatsComponent stats = sc.get(entity);
        WeaponComponent weapon = wc.get(entity);
        PlayerComponent player = pc.get(entity);
        BulletDataComponent bulletData = bm.get(entity);

        weapon.shootTimer += deltaTime;

        if (player.isShooting && weapon.shootTimer >= weapon.fireRate) {
            for (int i = 0; i < weapon.projectileCount; i++) {
                shootAndCreateBullet(stats, weapon, transform, (float) i, bulletData);
            }
            weapon.shootTimer = 0;
        }
    }

    private void shootAndCreateBullet(StatsComponent stats, WeaponComponent weapon, TransformComponent transform,
                                      float i, BulletDataComponent bulletData) {
        // 1. Calculate the starting angle (in degrees)
        float baseAngleDeg = transform.rotation;
        float angleOffset = 0;
        // 'i' is your current loop index
        if (weapon.projectileCount > 1) {
            float fraction = i / (weapon.projectileCount - 1);
            angleOffset = (fraction - 0.5f) * weapon.spreadAngle;
        }
        // Apply the offset
        float finalAngle = baseAngleDeg + angleOffset;

        entityFactory.createBullet(transform, weapon, bulletData, stats, finalAngle);
    }
}
