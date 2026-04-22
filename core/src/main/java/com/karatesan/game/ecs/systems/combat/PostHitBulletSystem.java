package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.perks.PierceMarkerComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class PostHitBulletSystem extends IteratingSystem implements PausableSystem {
    private final ComponentMapper<PardonedComponent> pardonMapper = ComponentMapper.getFor(PardonedComponent.class);
    private final ComponentMapper<HitEventComponent> hitEventMapper = ComponentMapper.getFor(HitEventComponent.class);
    private final ComponentMapper<PierceMarkerComponent> piercerMarkMap = ComponentMapper.getFor(
        PierceMarkerComponent.class);

    public PostHitBulletSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        HitEventComponent hitEventComponent = hitEventMapper.get(eventEntity);
        //if bullet is pardoned (ie after ricochet)
        if (pardonMapper.has(hitEventComponent.bullet)) {
            hitEventComponent.bullet.remove(PardonedComponent.class);
            //if bullet pierced
        } else if (piercerMarkMap.has(hitEventComponent.bullet)) {
            hitEventComponent.bullet.remove(PierceMarkerComponent.class);
            //if no special effect - mark dead
        } else {
            hitEventComponent.bullet.add(getEngine().createComponent(DeadComponent.class));
        }
        getEngine().removeEntity(eventEntity);
    }
}
