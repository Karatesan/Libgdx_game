package com.karatesan.game.ecs.systems.perks;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.BulletHitEvent;
import com.karatesan.game.ecs.components.perks.PierceComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.perks.PierceMarkerComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.utility.PausableSystem;

public class PiercePerkSystem extends IteratingSystem implements PausableSystem {

    public PiercePerkSystem() {
        super(Family.all(HitEventComponent.class, BulletHitEvent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hitEvent = Mappers.hitEvent.get(entity);
        PierceComponent pierceComponent = Mappers.pierce.get(hitEvent.bullet);

        if (pierceComponent == null || pierceComponent.pierceCount == 0) {
            return;
        }
        if (pierceComponent.lastHit == null || pierceComponent.lastHit != hitEvent.targetEntity) {
            pierceComponent.pierceCount--;
            pierceComponent.lastHit = hitEvent.targetEntity;
            hitEvent.rawDamage *= pierceComponent.pierceDamageRetention;
            hitEvent.bullet.add(getEngine().createComponent(PierceMarkerComponent.class));
        }
    }
}
