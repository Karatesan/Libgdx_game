package com.karatesan.game.data.perk;

public enum PerkEffectType {
    // ── Working now ──────────────────────────
    STAT_MODIFIER,
    TRAIT_ADDITION,

    // ── Parsed but ignored until systems exist ──
    STAT_OVERRIDE,
    ON_KILL_EFFECT,
    ON_CRIT_EFFECT,
    ON_EXPLOSION_EFFECT,
    ON_KILL_STACK,
    CONDITIONAL_BUFF,
    PASSIVE_TRIGGER,
    STANDING_STILL_STACK,
    SHOT_COUNTER_EFFECT,
    RANDOM_BULLET_EFFECT,
    PERIODIC_EFFECT,
    PERIODIC_RANDOM_BUFF
}
