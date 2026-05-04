package com.karatesan.game.ecs.systems.combat.temp;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.event.HitEventComponent;

public class CombatResolutionSystem extends IteratingSystem {
    public CombatResolutionSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }
}
