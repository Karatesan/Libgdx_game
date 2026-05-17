package com.karatesan.game.ecs.logic.combat.projectile;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;

public final class ProjectileImpactContext {
    public Engine engine;
    public Entity eventEntity;
    public HitEventComponent hit;

    public Entity bullet;
    public Entity target;
    public DamagePayloadComponent payload;

    public void set(
        Engine engine,
        Entity eventEntity,
        HitEventComponent hit,
        Entity bullet,
        DamagePayloadComponent payload) {

        this.engine = engine;
        this.eventEntity = eventEntity;
        this.hit = hit;
        this.bullet = bullet;
        this.target = hit.target;
        this.payload = payload;
    }

    public void clear() {
        engine = null;
        eventEntity = null;
        hit = null;
        bullet = null;
        target = null;
        payload = null;
    }
}
