package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ContactDamageComponent implements Component, Pool.Poolable {
    public float damage;

    @Override
    public void reset() {
        damage = 0;
    }
}
