package com.karatesan.game.data.perk;

public class PerkOffer {
    public final PerkDefinition definition;
    public final int nextLevel;
    public final PerkLevel levelData;

    public PerkOffer(PerkDefinition definition, int nextLevel) {
        this.definition = definition;
        this.nextLevel = nextLevel;
        this.levelData = definition.getLevel(nextLevel);
    }
}
