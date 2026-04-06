package com.karatesan.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InvincibilityComponent implements Component, Poolable {
    public float timer = .5f;

    @Override
    public void reset() {
        timer = .5f;
    }
}
