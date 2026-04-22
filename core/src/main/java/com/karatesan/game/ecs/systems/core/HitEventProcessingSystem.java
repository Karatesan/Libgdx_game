package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.factory.EntityFactory;

public class HitEventProcessingSystem extends IteratingSystem {
    private final ComponentMapper<HitEventComponent> em = ComponentMapper.getFor(HitEventComponent.class);
    private final ComponentMapper<HealthComponent> healthM = ComponentMapper.getFor(HealthComponent.class);

    private final EntityFactory entityFactory;

    public HitEventProcessingSystem(EntityFactory entityFactory) {
        super(Family.all(HitEventComponent.class).get());
        this.entityFactory = entityFactory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent event = em.get(entity);
        entityFactory.createDamageText(event.targetEntity, event.damage, event.isCrit);
        //Damage enemy
        HealthComponent eHealth = healthM.get(event.targetEntity);
        eHealth.currentHp -= event.damage;
        // 3. Check if Enemy died
        if (eHealth.currentHp <= 0) {
            event.targetEntity.add(getEngine().createComponent(DeathEventComponent.class));
        }
    }
}
