package com.karatesan.game.ecs.components.core;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class LifeTimeComponent implements Component, Pool.Poolable {
    public float timer;
    public float maxTime;

    @Override
    public void reset() {
        timer = 0f;
        maxTime = 0f;
    }
}
