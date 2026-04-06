package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.LifeTimeComponent;
import com.karatesan.game.ecs.components.UI.FloatingTextComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;

public class LifeTimeSystem extends IteratingSystem {
    private final ComponentMapper<LifeTimeComponent> lm = ComponentMapper.getFor(LifeTimeComponent.class);
    private final ComponentMapper<FloatingTextComponent> tm = ComponentMapper.getFor(FloatingTextComponent.class);

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
