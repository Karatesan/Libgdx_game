package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.UI.FloatingTextComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;

public class FloatingTextSystem extends IteratingSystem {
    private final ComponentMapper<FloatingTextComponent> ftm = ComponentMapper.getFor(FloatingTextComponent.class);
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

    public FloatingTextSystem() {
        super(Family.all(FloatingTextComponent.class, TransformComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        FloatingTextComponent text = ftm.get(entity);
        TransformComponent textTransform = tm.get(entity);

        Entity anchorEntity = text.anchorEntity;
        if (anchorEntity != null && tm.has(anchorEntity)){
            TransformComponent anchorTransform = tm.get(anchorEntity);
            textTransform.x = anchorTransform.x;
            text.offsetY += 5f * deltaTime;
            textTransform.y = anchorTransform.y + anchorTransform.size + text.offsetY;

        }
    }
}
