package com.karatesan.game.perks;

import java.util.List;

public class PerkDefinition {
    public String id;
    public String name;
    public String description;
    public Rarity rarity;
    public int maxLevel;
    public PerkLevel[] levels;

    public PerkLevel getLevel(int level) {
        return levels[level - 1];
    }
}

