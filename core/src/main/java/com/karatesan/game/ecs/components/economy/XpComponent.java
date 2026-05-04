package com.karatesan.game.ecs.components.economy;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class XpComponent implements Component, Poolable {
    public int value = 1;

    @Override
    public void reset() {
        value = 1;
    }
}
