package com.karatesan.game.ecs.systems.combat.perks;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.BulletComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.perks.PierceMarkerComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class PiercePerkSystem extends IteratingSystem implements PausableSystem {

    private final ComponentMapper<HitEventComponent> hm = ComponentMapper.getFor(HitEventComponent.class);
    private final ComponentMapper<BulletComponent> bm = ComponentMapper.getFor(BulletComponent.class);

    public PiercePerkSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hitEvent = hm.get(entity);
        BulletComponent bulletData = bm.get(hitEvent.bullet);
        if (bulletData.pierceCount == 0) {
            return;
        }
        if (bulletData.lastHit == null || bulletData.lastHit != hitEvent.targetEntity) {
            HitEventComponent hitEventComponent = hm.get(entity);
            bulletData.pierceCount--;
            bulletData.lastHit = hitEvent.targetEntity;
            hitEventComponent.damage *= .8f;
            hitEventComponent.bullet.add(getEngine().createComponent(PierceMarkerComponent.class));
        }
    }
}
