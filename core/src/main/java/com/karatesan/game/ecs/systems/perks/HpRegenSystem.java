package com.karatesan.game.ecs.systems.perks;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.stats.HealthComponent;

public class HpRegenSystem extends IteratingSystem {
    public HpRegenSystem() {
        super(Family.all(HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HealthComponent healthComponent = Mappers.health.get(entity);
        if (healthComponent.hpRegen > 0 && healthComponent.currentHp < healthComponent.maxHp) {
            healthComponent.currentHp = Math.min(healthComponent.maxHp,
                healthComponent.currentHp + healthComponent.hpRegen * deltaTime);
        }
    }
}
