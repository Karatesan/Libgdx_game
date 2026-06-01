package com.karatesan.game.ecs.systems.perks;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.event.StatsRecalculationFlag;
import com.karatesan.game.ecs.components.perks.LastStandComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;

public class ConditionalBuffSystem extends IteratingSystem {

    public ConditionalBuffSystem() {
        super(Family.all(LastStandComponent.class, HealthComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LastStandComponent lastStand = Mappers.lastStand.get(entity);
        HealthComponent health = Mappers.health.get(entity);
        boolean previousState = lastStand.isActive;

        lastStand.isActive = health.currentHp / health.maxHp <= lastStand.hpThresholdActivation;

        if (previousState != lastStand.isActive) {
            entity.add(getEngine().createComponent(StatsRecalculationFlag.class));
        }
    }
}
