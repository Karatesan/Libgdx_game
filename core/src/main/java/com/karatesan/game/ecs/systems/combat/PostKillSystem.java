package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.HitOutcome;
import com.karatesan.game.ecs.components.combat.hit.ResolvedHitComponent;
import com.karatesan.game.ecs.components.perks.LifeStealComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;

public class PostKillSystem extends IteratingSystem {

    public PostKillSystem() {
        super(Family.all(HitEventComponent.class, ResolvedHitComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hitEvent = Mappers.hitEvent.get(entity);

        if (hitEvent.outcome.equals(HitOutcome.KILLED) && Mappers.player.has(hitEvent.attacker)) {
            LifeStealComponent lifeSteal = Mappers.lifeSteal.get(hitEvent.attacker);
            if (lifeSteal != null) {
                DebugDisplay.logDebug(String.valueOf(lifeSteal.flatHpPerKill));
                HealthComponent health = Mappers.health.get(hitEvent.attacker);
                health.currentHp = Math.min(health.maxHp, health.currentHp + lifeSteal.flatHpPerKill);
            }
        }
        // Mark the event entity for cleanup
        entity.add(getEngine().createComponent(PendingRemovalComponent.class));
    }
}

