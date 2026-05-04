package com.karatesan.game.ecs.systems.economy;


import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.karatesan.game.data.blueprints.PlayerBlueprint;
import com.karatesan.game.data.blueprints.WeaponBlueprint;
import com.karatesan.game.data.registry.BlueprintRegistry;
import com.karatesan.game.data.registry.PerkRegistry;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.components.stats.HealthComponent;
import com.karatesan.game.ecs.components.combat.ProjectileTemplateComponent;
import com.karatesan.game.ecs.components.weapon.WeaponComponent;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.event.StatsRecalculationFlag;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.stats.DefenseStatsComponent;
import com.karatesan.game.ecs.components.stats.OffensiveStatsComponent;
import com.karatesan.game.ecs.components.stats.UtilityStatsComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.data.perk.PerkDefinition;
import com.karatesan.game.data.perk.PerkEffect;
import com.karatesan.game.data.perk.PerkEffectType;
import com.karatesan.game.data.perk.PerkLevel;

public class StatRecalculationSystem extends EntitySystem {

    private final GameContext context;
    private final GameConfig config;
    private final BlueprintRegistry blueprints;
    private final PerkRegistry perkRegistry;

    private final ObjectMap<String, Float> statDeltas = new ObjectMap<>();
    private final ObjectMap<String, Float> traitDeltas = new ObjectMap<>();

    private static final float DELTA_DISPLAY_SECONDS = 10f;
    private final StringBuilder deltaSb = new StringBuilder(48);

    //FOR DEBUG
    private static final int SNAPSHOT_SIZE = 22;
    private final float[] snapshot = new float[SNAPSHOT_SIZE];

    public StatRecalculationSystem(GameContext context, GameConfig config, BlueprintRegistry blueprints,
                                   PerkRegistry perkRegistry) {
        this.context = context;
        this.config = config;
        this.blueprints = blueprints;
        this.perkRegistry = perkRegistry;
    }

    @Override
    public void update(float deltaTime) {
        Entity player = context.getPlayer();
        if (player == null) return;
        if (!Mappers.recalcFlag.has(player)) return;

        recalculate(player);
        player.remove(StatsRecalculationFlag.class);
    }

    private void recalculate(Entity player) {
        // ── 0. Grab components ──────────────────────────────

        WeaponComponent wpn = Mappers.weapon.get(player);
        OffensiveStatsComponent off = Mappers.offense.get(player);
        DefenseStatsComponent def = Mappers.defense.get(player);
        UtilityStatsComponent util = Mappers.utility.get(player);
        MovementComponent mov = Mappers.movement.get(player);
        ProjectileTemplateComponent tmpl = Mappers.template.get(player);
        HealthComponent hp = Mappers.health.get(player);

        // ── 0b. Snapshot current values if debug overlay is on ──

        boolean logging = DebugDisplay.isEnabled();
        if (logging) snapshotStats(wpn, off, def, util, mov, tmpl, hp);

        // ── 1. Load base values ─────────────────────────────────

        PlayerBlueprint pb = blueprints.getPlayer();
        WeaponBlueprint wb = blueprints.getWeapon();

        // ── 2. Clear accumulators ───────────────────────────────

        statDeltas.clear();
        traitDeltas.clear();

        // ── 3. Accumulate all perk effects ──────────────────────

        PerkInventoryComponent inv = Mappers.perks.get(player);
        for (ObjectMap.Entry<String, Integer> entry : inv.acquiredPerks) {
            PerkDefinition perkDef = perkRegistry.get(entry.key);
            if (perkDef == null) continue;

            int currentLevel = entry.value;
            for (int lvl = 1; lvl <= currentLevel && lvl <= perkDef.maxLevel; lvl++) {
                PerkLevel levelData = perkDef.getLevel(lvl);
                if (levelData == null) continue;

                for (PerkEffect effect : levelData.effects) {
                    if (effect.type == PerkEffectType.STAT_MODIFIER) {
                        statDeltas.put(effect.target, statDeltas.get(effect.target, 0f) + effect.value);
                    } else if (effect.type == PerkEffectType.TRAIT_ADDITION) {
                        traitDeltas.put(effect.target, traitDeltas.get(effect.target, 0f) + effect.value);
                    }
                }
            }
        }

        // ── 4. Write weapon stats ───────────────────────────────

        wpn.minDamage = wb.minDamage + d("minDamage");
        wpn.maxDamage = wb.maxDamage + d("maxDamage");
        wpn.fireRate = wb.fireRate * (1f + d("fireRateMultiplier"));
        wpn.fireRate = MathUtils.clamp(wpn.fireRate, config.fireRateFloor, config.fireRateCeiling);
        wpn.projectileCount = wb.projectileCount + (int) d("projectileCount");
        wpn.spreadAngle = Math.max(0f, wb.spreadAngle + d("spreadAngle"));
        wpn.inaccuracy = Math.max(0f, wb.inaccuracy + d("inaccuracy"));
        wpn.projectileSpeed = wb.projectileSpeed + d("projectileSpeed");
        wpn.range = Math.max(50f, wb.range + d("range"));

        // ── 5. Write offensive stats ────────────────────────────

        off.critChance = MathUtils.clamp(pb.critChance + d("critChance"), config.critChanceFloor,
            config.critChanceCeiling);
        off.critMultiplier = Math.max(1f, pb.critMultiplier + d("critMultiplier"));
        off.damageMultiplier = pb.damageMultiplier + d("damageMultiplier");

        // ── 6. Write defensive stats ────────────────────────────

        def.armor = pb.armor + d("armor");
        def.dodgeChance = MathUtils.clamp(pb.dodgeChance + d("dodgeChance"), 0f, 0.5f);

        // ── 7. Write utility stats ──────────────────────────────

        util.pickupRadius = pb.pickupRadius * (1.0f + d("pickupRadius"));
        util.xpMultiplier = pb.xpMultiplier * (1.0f + d("xpMultiplier"));
        util.luck = pb.luck + d("luck");

        // ── 8. Write movement ───────────────────────────────────

        mov.maxSpeed = pb.moveSpeed * (1.0f +  d("moveSpeed"));

        // ── 9. Write projectile template ────────────────────────

        tmpl.pierceCount = (int) t("PIERCE");
        tmpl.pierceDamageRetention = config.pierceDamageRetention;
        tmpl.ricochetChance = t("RICOCHET_CHANCE");
        tmpl.ricochetCount = (int) t("RICOCHET_COUNT");
        tmpl.ricochetDamageRetention = config.ricochetBaseRetention + d("ricochetDamageRetention");
        tmpl.explosionRadius = config.explosionRadius;
        tmpl.explosionDamageRatio = config.explosionDamageRatio;
        tmpl.knockbackForce = d("knockbackForce");

        // ── 10. Handle maxHp proportionally ─────────────────────

        float prevMax = hp.maxHp;
        float newMax = pb.maxHp + d("maxHp");
        hp.maxHp = newMax;
        if (prevMax > 0f && prevMax != newMax) {
            hp.currentHp = hp.currentHp * (newMax / prevMax);
        }

        //hp regen
        hp.hpRegen = pb.hpRegen + d("hpRegen");

        //FOR DEBUG
        if (logging) logDeltas(wpn, off, def, util, mov, tmpl, hp);

    }

    // ── Delta helpers ───────────────────────────────────

    private void logDelta(String name, float oldVal, float newVal) {
        if (Float.compare(oldVal, newVal) == 0) return;
        deltaSb.setLength(0);
        appendFloat(oldVal, 2);
        deltaSb.append(" → ");
        appendFloat(newVal, 2);
        DebugDisplay.logTimed(name, deltaSb.toString(), DELTA_DISPLAY_SECONDS);
    }

    private void logDelta(String name, int oldVal, int newVal) {
        if (oldVal == newVal) return;
        deltaSb.setLength(0);
        deltaSb.append(oldVal).append(" → ").append(newVal);
        DebugDisplay.logTimed(name, deltaSb.toString(), DELTA_DISPLAY_SECONDS);
    }

    private void appendFloat(float value, int decimals) {
        if (value < 0) {
            deltaSb.append('-');
            value = -value;
        }
        int whole = (int) value;
        deltaSb.append(whole).append('.');
        float frac = value - whole;
        for (int i = 0; i < decimals; i++) {
            frac *= 10;
            deltaSb.append((int) frac % 10);
        }

    }

    private float d(String key) {return statDeltas.get(key, 0f);}

    private float t(String key) {return traitDeltas.get(key, 0f);}

    //FOR DEBUG

    private void snapshotStats(WeaponComponent wpn, OffensiveStatsComponent off, DefenseStatsComponent def,
                               UtilityStatsComponent util, MovementComponent mov, ProjectileTemplateComponent tmpl,
                               HealthComponent hp) {
        int i = 0;
        snapshot[i++] = wpn.minDamage;
        snapshot[i++] = wpn.maxDamage;
        snapshot[i++] = wpn.fireRate;
        snapshot[i++] = wpn.projectileCount;
        snapshot[i++] = wpn.spreadAngle;
        snapshot[i++] = wpn.projectileSpeed;
        snapshot[i++] = wpn.range;
        snapshot[i++] = off.critChance;
        snapshot[i++] = off.critMultiplier;
        snapshot[i++] = off.damageMultiplier;
        snapshot[i++] = def.armor;
        snapshot[i++] = def.dodgeChance;
        snapshot[i++] = util.pickupRadius;
        snapshot[i++] = util.xpMultiplier;
        snapshot[i++] = util.luck;
        snapshot[i++] = mov.maxSpeed;
        snapshot[i++] = tmpl.pierceCount;
        snapshot[i++] = tmpl.ricochetChance;
        snapshot[i++] = tmpl.ricochetCount;
        snapshot[i++] = tmpl.ricochetDamageRetention;
        snapshot[i++] = tmpl.knockbackForce;
        snapshot[i] = hp.maxHp;
    }

    private void logDeltas(WeaponComponent wpn, OffensiveStatsComponent off, DefenseStatsComponent def,
                           UtilityStatsComponent util, MovementComponent mov, ProjectileTemplateComponent tmpl,
                           HealthComponent hp) {
        int i = 0;
        logDelta("minDamage", snapshot[i++], wpn.minDamage);
        logDelta("maxDamage", snapshot[i++], wpn.maxDamage);
        logDelta("fireRate", snapshot[i++], wpn.fireRate);
        logDelta("projCount", (int) snapshot[i++], wpn.projectileCount);
        logDelta("spreadAngle", snapshot[i++], wpn.spreadAngle);
        logDelta("projSpeed", snapshot[i++], wpn.projectileSpeed);
        logDelta("range", snapshot[i++], wpn.range);
        logDelta("critChance", snapshot[i++], off.critChance);
        logDelta("critMult", snapshot[i++], off.critMultiplier);
        logDelta("dmgMult", snapshot[i++], off.damageMultiplier);
        logDelta("armor", snapshot[i++], def.armor);
        logDelta("dodgeChance", snapshot[i++], def.dodgeChance);
        logDelta("pickupRadius", snapshot[i++], util.pickupRadius);
        logDelta("xpMult", snapshot[i++], util.xpMultiplier);
        logDelta("luck", snapshot[i++], util.luck);
        logDelta("moveSpeed", snapshot[i++], mov.maxSpeed);
        logDelta("pierce", (int) snapshot[i++], tmpl.pierceCount);
        logDelta("ricoChance", snapshot[i++], tmpl.ricochetChance);
        logDelta("ricoCount", (int) snapshot[i++], tmpl.ricochetCount);
        logDelta("ricoRetention", snapshot[i++], tmpl.ricochetDamageRetention);
        logDelta("knockback", snapshot[i++], tmpl.knockbackForce);
        logDelta("maxHp", snapshot[i], hp.maxHp);
    }
}
