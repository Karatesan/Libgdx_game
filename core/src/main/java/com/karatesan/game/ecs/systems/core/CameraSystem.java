package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class CameraSystem extends EntitySystem implements PausableSystem {
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final OrthographicCamera camera;
    private final GameContext context;

    public CameraSystem(OrthographicCamera camera, GameContext context) {
        this.camera = camera;
        this.context = context;
    }

    @Override
    public void update(float deltaTime) {
        TransformComponent transformComponent = tm.get(context.getPlayer());
        camera.position.x = transformComponent.x;
        camera.position.y = transformComponent.y;
        camera.update();
    }

}
