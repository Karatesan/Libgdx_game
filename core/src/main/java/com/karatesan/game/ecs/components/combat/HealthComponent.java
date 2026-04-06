package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HealthComponent implements Component, Poolable {
    public float maxHp = 100f;
    public float currentHp = 100f;

    @Override
    public void reset() {
        maxHp = 100f;
        currentHp = 100f;
    }
}
