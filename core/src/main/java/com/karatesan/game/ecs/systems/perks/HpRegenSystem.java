package com.karatesan.game.ecs.systems.perks;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.stats.HealthComponent;
/**
 * Passively regenerates health over time for all entities with a {@link HealthComponent}.
 * <p>
 * The regeneration amount is calculated per frame using:
 * {@code hpRegen * hpRegenMultiplier * deltaTime}.
 * Health will never regenerate past the entity's {@code maxHp}.
 */
public class HpRegenSystem extends IteratingSystem {
    public HpRegenSystem() {
        super(Family.all(HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HealthComponent healthComponent = Mappers.health.get(entity);
        if (healthComponent.hpRegen > 0 && healthComponent.currentHp < healthComponent.maxHp) {
            // if we reduce hp regen by 40% (in perk its -0.4) the formula is hpRegen * (1 - 0.4) = 60% of original value
            healthComponent.currentHp = Math.min(healthComponent.maxHp,
                healthComponent.currentHp + healthComponent.hpRegen * healthComponent.hpRegenMultiplier * deltaTime);
        }
    }
}
