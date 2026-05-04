package com.karatesan.game.ecs.components.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class WeaponComponent implements Component, Poolable {
    public float minDamage;
    public float maxDamage;
    public float fireRate;
    public int projectileCount = 1;
    public float spreadAngle = 0;
    public float inaccuracy = 0;
    public float projectileSpeed;
    public float range;

    @Override
    public void reset() {

    }
}
