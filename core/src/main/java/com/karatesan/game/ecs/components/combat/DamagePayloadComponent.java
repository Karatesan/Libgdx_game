package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class DamagePayloadComponent implements Component, Pool.Poolable {
    public Entity owner;

    public float baseDamage;
    public float currentDamage;

    public float critChance;
    public float critMultiplier;

    @Override
    public void reset() {
        owner = null;
        baseDamage = 0f;
        currentDamage = 0f;
        critChance = 0f;
        critMultiplier = 1f;
    }
}

//bullet payload jest tworzony podczas tworzenia bulleta, jak leci ricochet to nie jest to juz robione i mamy domysln 0
