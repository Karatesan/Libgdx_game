package com.karatesan.game.ecs.systems.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;

public class PlayerInputSystem extends IteratingSystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<MovementComponent> mm = ComponentMapper.getFor(MovementComponent.class);
    private final ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);


    private final OrthographicCamera camera;
    private final Vector3 mousePos = new Vector3();

    public PlayerInputSystem(OrthographicCamera camera) {
        // Only process entities with the Player Tag, Transform, and Velocity
        super(Family.all(PlayerComponent.class, TransformComponent.class, VelocityComponent.class).get());
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        VelocityComponent velocity = vm.get(entity);
        MovementComponent movement = mm.get(entity);
        PlayerComponent player = pm.get(entity);

        processMovement(velocity, movement.maxSpeed);

        mousePos.x = Gdx.input.getX();
        mousePos.y = Gdx.input.getY();

        camera.unproject(mousePos);
        float angleInRadians = MathUtils.atan2(mousePos.y - transform.y, mousePos.x - transform.x);
        transform.rotation = angleInRadians * MathUtils.radiansToDegrees;

        player.isShooting = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    private void processMovement(VelocityComponent velocity, float speed) {
        float deltaX = 0;
        float deltaY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            deltaY += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            deltaY -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            deltaX += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            deltaX -= 1;
        }

        if (deltaX != 0 || deltaY != 0) {
            float len = Vector2.len(deltaX, deltaY);
            deltaX = deltaX / len;
            deltaY = deltaY / len;
        }

        velocity.x = deltaX * speed;
        velocity.y = deltaY * speed;
    }
}
