package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PerkChoiceComponent implements Component, Pool.Poolable {
    public String perkId;

    @Override
    public void reset() {
        perkId = null;
    }
}
