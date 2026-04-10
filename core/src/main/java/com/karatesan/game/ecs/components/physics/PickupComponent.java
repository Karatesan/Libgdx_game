package com.karatesan.game.ecs.components.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class PickupComponent implements Component, Poolable {
    public boolean isMagnetized = false;
    public float homingSpeed = 300f; // Speed at which it flies to player

    @Override
    public void reset() {
        isMagnetized = false;
        homingSpeed = 300f;
    }
}
