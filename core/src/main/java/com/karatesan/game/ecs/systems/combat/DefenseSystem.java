package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.stats.DefenseStatsComponent;

public class DefenseSystem extends IteratingSystem {

    public DefenseSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hitEventComponent = Mappers.hitEvent.get(entity);
        DefenseStatsComponent defenseStatsComponent = Mappers.defense.get(hitEventComponent.targetEntity);

        if (defenseStatsComponent.dodgeChance > 0) {
            hitEventComponent.isDodged = MathUtils.random() <= defenseStatsComponent.dodgeChance;
        }
        if (!hitEventComponent.isDodged) {
            hitEventComponent.finalDamage = Math.max(hitEventComponent.finalDamage - defenseStatsComponent.armor, 0);
        }
    }
}
