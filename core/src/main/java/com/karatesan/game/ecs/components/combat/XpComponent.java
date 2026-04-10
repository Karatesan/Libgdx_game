package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class XpComponent implements Component, Poolable {
    public float value = 1f;

    @Override
    public void reset() {
        value = 1f;
    }
}
