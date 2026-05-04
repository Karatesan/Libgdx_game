package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InvincibilityComponent implements Component, Poolable {
    public float timer;

    @Override
    public void reset() {
        timer = 0;
    }
}
