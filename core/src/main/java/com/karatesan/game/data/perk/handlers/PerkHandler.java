package com.karatesan.game.data.perk.handlers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.JsonValue;
import com.karatesan.game.data.perk.PerkLevel;

public interface PerkHandler {
    void create(JsonValue perkData);
    void onAcquire(Entity player, int level, PerkLevel[] levels, Engine engine);
}
