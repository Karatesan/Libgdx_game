package com.karatesan.game.ecs.systems.movement;

import com.karatesan.game.ecs.components.weapon.WeaponStateComponent;
import com.karatesan.game.ecs.Mappers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class PlayerInputSystem extends EntitySystem implements PausableSystem {

    private final OrthographicCamera camera;
    private final Vector3 mousePos = new Vector3();
    private final GameContext context;

    public PlayerInputSystem(OrthographicCamera camera, GameContext context) {
        this.camera = camera;
        this.context = context;
    }

    @Override
    public void update(float deltaTime) {
        Entity playerEntity = context.getPlayer();
        TransformComponent transform = Mappers.transform.get(playerEntity);
        VelocityComponent velocity = Mappers.velocity.get(playerEntity);
        MovementComponent movement = Mappers.movement.get(playerEntity);

        processMovement(velocity, movement.maxSpeed);

        mousePos.x = Gdx.input.getX();
        mousePos.y = Gdx.input.getY();

        camera.unproject(mousePos);
        float angleInRadians = MathUtils.atan2(mousePos.y - transform.y, mousePos.x - transform.x);
        transform.rotation = angleInRadians * MathUtils.radiansToDegrees;

        WeaponStateComponent weaponStateComponent = Mappers.weaponState.get(playerEntity);
        weaponStateComponent.isShooting = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
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
