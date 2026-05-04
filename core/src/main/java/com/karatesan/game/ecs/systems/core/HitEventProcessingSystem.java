package com.karatesan.game.ecs.systems.core;

import static com.karatesan.game.ecs.Mappers.*;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.factory.FloatingTextStyle;

public class HitEventProcessingSystem extends IteratingSystem {

    private final EntityFactory entityFactory;

    public HitEventProcessingSystem(EntityFactory entityFactory) {
        super(Family.all(HitEventComponent.class).get());
        this.entityFactory = entityFactory;
    }

    //TODO maybe not create here damage text - as later it might be dodged or reduced by armor
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent event = hitEvent.get(entity);
        FloatingTextStyle style;
        //Dodged
        if (event.isDodged) {
            entityFactory.createFloatingText(event.targetEntity, "DODGE", FloatingTextStyle.DODGE);
        } else {
            if (event.bullet == null) style = FloatingTextStyle.PLAYER_HIT;
            else if (event.isCrit) style = FloatingTextStyle.CRIT;
            else if (event.finalDamage < event.rawDamage) style = FloatingTextStyle.ARMORED;
            else style = FloatingTextStyle.DAMAGE;

            entityFactory.createDamageText(event.targetEntity, event.finalDamage, style);

            //Damage entity
            HealthComponent eHealth = health.get(event.targetEntity);
            eHealth.currentHp -= event.finalDamage;
            // 3. Check if Enemy died
            if (eHealth.currentHp <= 0) {
                event.targetEntity.add(getEngine().createComponent(DeathEventComponent.class));
            }
        }
    }
}


