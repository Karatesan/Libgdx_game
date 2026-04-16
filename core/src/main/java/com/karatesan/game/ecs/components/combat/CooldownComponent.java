package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class CooldownComponent implements Component, Pool.Poolable {

    public float cooldown;
    public float remainingCooldown = 0f;

    @Override
    public void reset() {
        cooldown = 0f;
        remainingCooldown = 0f;
    }
}
