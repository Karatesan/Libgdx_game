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
    private final ComponentMapper<HitEventComponent> em = ComponentMapper.getFor(HitEventComponent.class);

    public BulletLifecycleSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        HitEventComponent hitEventComponent = em.get(eventEntity);
        if (pm.has(hitEventComponent.bullet)) {
            hitEventComponent.bullet.remove(PardonedComponent.class);
        } else {
            hitEventComponent.bullet.add(getEngine().createComponent(DeadComponent.class));
        }
        getEngine().removeEntity(eventEntity);
    }
}
