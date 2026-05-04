package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.combat.ProjectileTemplateComponent;
import com.karatesan.game.ecs.components.weapon.WeaponComponent;
import com.karatesan.game.ecs.components.weapon.WeaponStateComponent;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.stats.OffensiveStatsComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.utility.PausableSystem;

public class WeaponSystem extends EntitySystem implements PausableSystem {

    private final EntityFactory entityFactory;
    private final GameContext context;

    public WeaponSystem(EntityFactory entityFactory, GameContext context) {
        this.entityFactory = entityFactory;
        this.context = context;
    }

    @Override
    public void update(float deltaTime) {
        Entity player = context.getPlayer();
        TransformComponent transform = Mappers.transform.get(player);
        OffensiveStatsComponent stats = Mappers.offense.get(player);
        WeaponComponent weapon = Mappers.weapon.get(player);
        ProjectileTemplateComponent bulletData = Mappers.template.get(player);
        WeaponStateComponent weaponStateComponent = Mappers.weaponState.get(player);

        weaponStateComponent.shootTimer += deltaTime;

        if (weaponStateComponent.isShooting && weaponStateComponent.shootTimer >= weapon.fireRate) {
            for (int i = 0; i < weapon.projectileCount; i++) {
                shootAndCreateBullet(stats, weapon, transform, (float) i, bulletData);
            }
            weaponStateComponent.shootTimer = 0;
        }
    }

    private void shootAndCreateBullet(OffensiveStatsComponent stats, WeaponComponent weapon,
                                      TransformComponent transform, float i, ProjectileTemplateComponent bulletData) {
        float baseAngleDeg = transform.rotation;
        float angleOffset = 0;

        if (weapon.projectileCount > 1) {
            float fraction = i / (weapon.projectileCount - 1);
            angleOffset = (fraction - 0.5f) * weapon.spreadAngle;
        }

        // Inaccuracy: random drift per bullet
        float drift = 0;
        if (weapon.inaccuracy > 0) {
            drift = MathUtils.random(-weapon.inaccuracy, weapon.inaccuracy);
        }

        float finalAngle = baseAngleDeg + angleOffset + drift;

        entityFactory.createBullet(transform, weapon, bulletData, stats, finalAngle);
    }
}
