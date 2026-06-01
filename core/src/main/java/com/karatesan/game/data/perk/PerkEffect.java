package com.karatesan.game.data.perk;

public class PerkEffect {
    public PerkEffectType type;
    public String target;
    public float value;

    public PerkEffect(PerkEffectType type, String target, float value) {
        this.type = type;
        this.target = target;
        this.value = value;
    }
}
