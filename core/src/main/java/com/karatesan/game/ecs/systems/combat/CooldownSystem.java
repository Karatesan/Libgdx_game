package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.CooldownComponent;

public class CooldownSystem extends IteratingSystem {
    private final ComponentMapper<CooldownComponent> componentMapper = ComponentMapper.getFor(CooldownComponent.class);

    public CooldownSystem() {
        super(Family.all(CooldownComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CooldownComponent cooldown = componentMapper.get(entity);
        float remainingCooldown = cooldown.remainingCooldown;
        cooldown.remainingCooldown = Math.max(remainingCooldown - deltaTime, 0f);
    }
}
