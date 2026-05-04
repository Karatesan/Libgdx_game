package com.karatesan.game.ecs.components.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class WeaponStateComponent implements Component, Pool.Poolable {
    public float shootTimer;
    public boolean isShooting = false;
    @Override
    public void reset() {
        shootTimer = 0;
        isShooting = false;
    }
}
