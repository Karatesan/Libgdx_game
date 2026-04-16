package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.ObjectMap;

public class PerkInventoryComponent implements Component {
    public final ObjectMap<String, Integer> acquiredPerks = new ObjectMap<>();

    public int getLevel(String perkId) {
        return acquiredPerks.get(perkId, 0);
    }

    public boolean isMaxed(String perkId, int maxLevel) {
        return getLevel(perkId) >= maxLevel;
    }
}
