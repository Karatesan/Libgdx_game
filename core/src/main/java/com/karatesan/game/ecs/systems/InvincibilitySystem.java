package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.InvincibilityComponent;

public class InvincibilitySystem extends IteratingSystem {
    private final ComponentMapper<InvincibilityComponent> im = ComponentMapper.getFor(InvincibilityComponent.class);

    public InvincibilitySystem() {
        super(Family.all(InvincibilityComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InvincibilityComponent invincibility = im.get(entity);
        invincibility.timer -= deltaTime;

        if (invincibility.timer <= 0) {
            entity.remove(InvincibilityComponent.class);
        }
    }
}
