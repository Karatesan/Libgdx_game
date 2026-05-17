package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.combat.InvincibilityComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.HitOutcome;
import com.karatesan.game.ecs.components.combat.hit.HitSourceType;
import com.karatesan.game.ecs.components.combat.hit.ResolvedHitComponent;
import com.karatesan.game.ecs.components.stats.DefenseStatsComponent;
import com.karatesan.game.ecs.components.stats.HealthComponent;

public class CombatResolutionSystem extends IteratingSystem {

    private final GameConfig config;

    public CombatResolutionSystem(GameConfig config) {
        super(Family.all(HitEventComponent.class).exclude(ResolvedHitComponent.class).get());
        this.config = config;
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        HitEventComponent hit = Mappers.hitEvent.get(eventEntity);
        if (!isValid(hit)) {
            hit.outcome = HitOutcome.INVALID;
            markResolved(eventEntity);
            return;
        }

        if (Mappers.invincibility.has(hit.target)) {
            hit.finalDamage = 0f;
            hit.outcome = HitOutcome.IMMUNE;
            markResolved(eventEntity);
            return;
        }

        DefenseStatsComponent defense = Mappers.defense.get(hit.target);

        if (defense.dodgeChance > 0) {
            if (MathUtils.random() < defense.dodgeChance) {
                DebugDisplay.logDebug("DODGED");
                hit.finalDamage = 0f;
                hit.outcome = HitOutcome.DODGED;
                //without this when dodged player get instant hit by enemy
                if (Mappers.player.has(hit.target)) {
                    addInvincibility(hit.target);
                }
                markResolved(eventEntity);
                return;
            }
        }

        applyCrit(hit);
        applyArmor(hit, defense);
        applyHealthDamage(hit);
        markResolved(eventEntity);
    }

    private boolean isValid(HitEventComponent hit) {
        return hit.target != null && Mappers.health.has(hit.target) && !Mappers.dead.has(hit.target);
    }

    private void applyCrit(HitEventComponent hit) {
        if (hit.sourceType != HitSourceType.PROJECTILE && hit.sourceType != HitSourceType.EXPLOSION) {
            return;
        }

        DamagePayloadComponent payload = Mappers.damage.get(hit.source);
        if (payload == null) {
            return;
        }

        if (MathUtils.random() < payload.critChance) {
            hit.crit = true;
            hit.finalDamage *= payload.critMultiplier;
        }
    }

    private void applyArmor(HitEventComponent hit, DefenseStatsComponent defense) {
        if (defense == null) {
            return;
        }
        hit.finalDamage = Math.max(0f, hit.finalDamage - defense.armor);
    }

    private void applyHealthDamage(HitEventComponent hit) {
        HealthComponent health = Mappers.health.get(hit.target);
        health.currentHp -= hit.finalDamage;
        boolean isPlayer = Mappers.player.has(hit.target);

        if (health.currentHp <= 0f) {
            hit.lethal = true;
            hit.outcome = HitOutcome.KILLED;

            if (isPlayer) {
                FatalDamageComponent fatal = getEngine().createComponent(FatalDamageComponent.class);
                hit.target.add(fatal);
            } else {
                DeathEventComponent death = getEngine().createComponent(DeathEventComponent.class);
                death.killer = hit.attacker;
                death.source = hit.source;
                death.sourceType = hit.sourceType;
                death.x = hit.x;
                death.y = hit.y;
                hit.target.add(death);
            }
        } else {
            if (isPlayer) {
                addInvincibility(hit.target);
            }
            hit.outcome = HitOutcome.DAMAGED;
        }
    }

    private void markResolved(Entity eventEntity) {
        eventEntity.add(getEngine().createComponent(ResolvedHitComponent.class));
    }

    private void addInvincibility(Entity player) {
        InvincibilityComponent invincibility = getEngine().createComponent(InvincibilityComponent.class);
        invincibility.timer = config.iFramesDuration;
        player.add(invincibility);
    }
}
