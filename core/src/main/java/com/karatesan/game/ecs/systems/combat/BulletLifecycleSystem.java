package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.core.LifeTimeComponent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class BulletLifecycleSystem extends IteratingSystem implements PausableSystem {
    private final ComponentMapper<PardonedComponent> pm = ComponentMapper.getFor(PardonedComponent.class);

    public BulletLifecycleSystem() {
        super(Family.all(BulletComponent.class, HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(HitEventComponent.class);
        if (pm.has(entity)) {
            entity.remove(PardonedComponent.class);
        } else {
            entity.add(getEngine().createComponent(DeadComponent.class));
        }
    }
}
