package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;

public class BulletLifecycleSystem extends IteratingSystem {
    private final ComponentMapper<HitEventComponent> hm = ComponentMapper.getFor(HitEventComponent.class);

    public BulletLifecycleSystem() {
        super(Family.all(BulletComponent.class, HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(HitEventComponent.class);
        entity.add(getEngine().createComponent(DeadComponent.class));

    }
}
