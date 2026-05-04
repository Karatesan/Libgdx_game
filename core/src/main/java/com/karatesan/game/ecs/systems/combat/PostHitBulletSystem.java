package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.BulletHitEvent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.perks.PierceMarkerComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class PostHitBulletSystem extends IteratingSystem implements PausableSystem {

    public PostHitBulletSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        HitEventComponent hitEventComponent = Mappers.hitEvent.get(eventEntity);
        //Enemy case
        if(hitEventComponent.bullet != null) {
            //if bullet is pardoned (ie after ricochet)
            if (Mappers.pardon.has(hitEventComponent.bullet)) {
                hitEventComponent.bullet.remove(PardonedComponent.class);
                //if bullet pierced
            } else if (Mappers.pierceMarker.has(hitEventComponent.bullet)) {
                hitEventComponent.bullet.remove(PierceMarkerComponent.class);
                //if no special effect - mark dead
            } else {
                hitEventComponent.bullet.add(getEngine().createComponent(DeadComponent.class));
            }
        }
        //both enemy and player
        getEngine().removeEntity(eventEntity);
    }
}
