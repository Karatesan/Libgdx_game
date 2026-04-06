package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class DamagePayloadComponent implements Component, Poolable {
    public float damage;
    public boolean isCrit;

    @Override
    public void reset() {
        damage = 0;
        isCrit = false;
    }
}
