package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;

public class CameraSystem extends IteratingSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final OrthographicCamera camera;

    public CameraSystem(OrthographicCamera camera) {
        super(Family.all(PlayerComponent.class, TransformComponent.class).get());
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = tm.get(entity);
        camera.position.x = transformComponent.x;
        camera.position.y = transformComponent.y;
        camera.update();
    }
}
