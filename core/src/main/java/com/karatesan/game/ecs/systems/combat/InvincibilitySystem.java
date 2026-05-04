package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.InvincibilityComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class InvincibilitySystem extends IteratingSystem implements PausableSystem {
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
