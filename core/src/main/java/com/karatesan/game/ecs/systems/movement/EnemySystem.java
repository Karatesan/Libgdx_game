package com.karatesan.game.ecs.systems.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class EnemySystem extends IteratingSystem implements PausableSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<MovementComponent> mm = ComponentMapper.getFor(MovementComponent.class);

    private final GameContext context;

    public EnemySystem(GameContext context) {
        super(Family.all(EnemyComponent.class, TransformComponent.class, VelocityComponent.class).get());
        this.context = context;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Entity player = context.getPlayer();
        if (player == null) return; // Don't move if there is no player

        TransformComponent enemyPos = tm.get(entity);
        VelocityComponent enemyVel = vm.get(entity);
        TransformComponent playerPos = tm.get(player);
        MovementComponent movement = mm.get(entity);

        // Calculate angle to player
        float angleRad = MathUtils.atan2(playerPos.y - enemyPos.y, playerPos.x - enemyPos.x);

        // Move toward player at 100 units per second
        enemyVel.x = MathUtils.cos(angleRad) * movement.maxSpeed;
        enemyVel.y = MathUtils.sin(angleRad) * movement.maxSpeed;
    }
}
