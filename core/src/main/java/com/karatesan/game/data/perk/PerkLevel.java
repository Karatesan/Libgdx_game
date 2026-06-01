package com.karatesan.game.data.perk;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public class PerkLevel {
    public int level;
    public String levelUpDescription;
    public Array<PerkEffect> statEffects;    // permanent stat/trait deltas
    public JsonValue behavioralConfig;       // raw effects array for handler; null if none
}
