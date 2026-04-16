package com.karatesan.game.ecs.systems.combat.perks;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.perks.PierceComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public class PiercePerkSystem extends IteratingSystem {

    public PiercePerkSystem() {
        super(Family.all(PierceComponent.class, PlayerComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }
}
