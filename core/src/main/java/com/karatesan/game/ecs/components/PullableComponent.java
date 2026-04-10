package com.karatesan.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PullableComponent implements Component, Pool.Poolable {
    public float pullSpeedMultiplier = 1f; // Heavy items can be pulled slower!
    @Override public void reset() { pullSpeedMultiplier = 1f; }
}
