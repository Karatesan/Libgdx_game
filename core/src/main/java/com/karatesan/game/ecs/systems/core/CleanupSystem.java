package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.tag.DeadComponent;

public class CleanupSystem extends IteratingSystem {

    public CleanupSystem() {
        // Look for ANYTHING that is marked as Dead
        super(Family.all(DeadComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Safely remove the entity from the game and return its components to the pool
        getEngine().removeEntity(entity);
    }
}
