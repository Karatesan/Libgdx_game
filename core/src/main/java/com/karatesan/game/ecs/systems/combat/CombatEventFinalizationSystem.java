package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.ResolvedHitComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;

public final class CombatEventFinalizationSystem extends IteratingSystem {

    public CombatEventFinalizationSystem() {
        super(Family.all(HitEventComponent.class, ResolvedHitComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hitEvent = Mappers.hitEvent.get(entity);
        // Mark the event entity for cleanup
        entity.add(getEngine().createComponent(PendingRemovalComponent.class));
    }
}
