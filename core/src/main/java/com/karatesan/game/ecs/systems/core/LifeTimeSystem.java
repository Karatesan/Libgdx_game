package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.core.LifeTimeComponent;
import com.karatesan.game.ecs.components.render.FloatingTextComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;

public class LifeTimeSystem extends IteratingSystem implements PausableSystem{
    private final ComponentMapper<LifeTimeComponent> lm = ComponentMapper.getFor(LifeTimeComponent.class);

    public LifeTimeSystem() {
        super(Family.all(LifeTimeComponent.class, FloatingTextComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LifeTimeComponent lifeTimeComponent = lm.get(entity);
        lifeTimeComponent.timer -= deltaTime;

        if (lifeTimeComponent.timer <= 0) {
            entity.add(getEngine().createComponent(DeadComponent.class));
        }
    }
}
