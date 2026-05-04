package com.karatesan.game.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.karatesan.game.ecs.components.combat.*;
import com.karatesan.game.ecs.components.economy.XpComponent;
import com.karatesan.game.ecs.components.stats.*;
import com.karatesan.game.ecs.components.weapon.ProjectileDistanceTravelledComponent;
import com.karatesan.game.ecs.components.weapon.WeaponComponent;
import com.karatesan.game.ecs.components.weapon.WeaponStateComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.economy.LevelDataComponent;
import com.karatesan.game.ecs.components.economy.LootDropComponent;
import com.karatesan.game.ecs.components.event.FatalDamageComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.event.StatsRecalculationFlag;
import com.karatesan.game.ecs.components.perks.*;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.MovementComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.physics.VelocityComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public final class Mappers {

    private Mappers() {}

    // ── Common ──────────────────────────────────────────
    public static final ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(
        TransformComponent.class);
    public static final ComponentMapper<VelocityComponent> velocity = ComponentMapper.getFor(VelocityComponent.class);
    public static final ComponentMapper<MovementComponent> movement = ComponentMapper.getFor(MovementComponent.class);
    public static final ComponentMapper<HealthComponent> health = ComponentMapper.getFor(HealthComponent.class);
    public static final ComponentMapper<HitboxComponent> hitbox = ComponentMapper.getFor(HitboxComponent.class);
    public static final ComponentMapper<ShapeComponent> shape = ComponentMapper.getFor(ShapeComponent.class);
    public static final ComponentMapper<ContactDamageComponent> contactDamage = ComponentMapper.getFor(
        ContactDamageComponent.class);
    public static final ComponentMapper<LootDropComponent> lootDrop = ComponentMapper.getFor(LootDropComponent.class);
    public static final ComponentMapper<OffensiveStatsComponent> offense = ComponentMapper.getFor(
        OffensiveStatsComponent.class);
    public static final ComponentMapper<DefenseStatsComponent> defense = ComponentMapper.getFor(
        DefenseStatsComponent.class);

    // ── Player ──────────────────────────────────────────
    public static final ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<ShootingStateComponent> shootState = ComponentMapper.getFor(
        ShootingStateComponent.class);
    public static final ComponentMapper<LevelDataComponent> level = ComponentMapper.getFor(LevelDataComponent.class);
    public static final ComponentMapper<UtilityStatsComponent> utility = ComponentMapper.getFor(
        UtilityStatsComponent.class);
    public static final ComponentMapper<PerkInventoryComponent> perks = ComponentMapper.getFor(
        PerkInventoryComponent.class);

    // ── Weapon ──────────────────────────────────────────
    public static final ComponentMapper<WeaponComponent> weapon = ComponentMapper.getFor(WeaponComponent.class);
    public static final ComponentMapper<WeaponStateComponent> weaponState = ComponentMapper.getFor(
        WeaponStateComponent.class);
    public static final ComponentMapper<ProjectileTemplateComponent> template = ComponentMapper.getFor(
        ProjectileTemplateComponent.class);

    // ── Projectile ──────────────────────────────────────
    public static final ComponentMapper<BulletComponent> bullet = ComponentMapper.getFor(BulletComponent.class);
    public static final ComponentMapper<DamagePayloadComponent> damage = ComponentMapper.getFor(
        DamagePayloadComponent.class);
    public static final ComponentMapper<PierceComponent> pierce = ComponentMapper.getFor(PierceComponent.class);
    public static final ComponentMapper<ProjectileDistanceTravelledComponent> distanceTravelled = ComponentMapper.getFor(
        ProjectileDistanceTravelledComponent.class);
    public static final ComponentMapper<RicochetComponent> ricochet = ComponentMapper.getFor(RicochetComponent.class);
//    public static final ComponentMapper<ExplosiveComponent> explosive =
//        ComponentMapper.getFor(ExplosiveComponent.class);
//    public static final ComponentMapper<KnockbackComponent> knockback =
//        ComponentMapper.getFor(KnockbackComponent.class);

    // ── Enemy ───────────────────────────────────────────
    public static final ComponentMapper<EnemyComponent> enemy = ComponentMapper.getFor(EnemyComponent.class);

    // ── Session ─────────────────────────────────────────
    public static final ComponentMapper<SessionComponent> session = ComponentMapper.getFor(SessionComponent.class);

    // ── Flags ───────────────────────────────────────────
//    public static final ComponentMapper<StatsRecalculationFlag> recalcFlag =
//        ComponentMapper.getFor(StatsRecalculationFlag.class);
    public static final ComponentMapper<LevelUpComponent> levelUp = ComponentMapper.getFor(LevelUpComponent.class);
    public static final ComponentMapper<FatalDamageComponent> fatalDamage = ComponentMapper.getFor(
        FatalDamageComponent.class);
    public static final ComponentMapper<DeadComponent> dead = ComponentMapper.getFor(DeadComponent.class);
    public static final ComponentMapper<InvincibilityComponent> invincibility = ComponentMapper.getFor(
        InvincibilityComponent.class);
    public static final ComponentMapper<PierceMarkerComponent> pierceMarker = ComponentMapper.getFor(
        PierceMarkerComponent.class);
    public static final ComponentMapper<PardonedComponent> pardon = ComponentMapper.getFor(PardonedComponent.class);
    public static final ComponentMapper<StatsRecalculationFlag> recalcFlag = ComponentMapper.getFor(
        StatsRecalculationFlag.class);
    public static final ComponentMapper<PerkChoiceComponent> perkChoice = ComponentMapper.getFor(
        PerkChoiceComponent.class);

    // ── XP ───────────────────────────────────────────
    public static final ComponentMapper<XpComponent> xp = ComponentMapper.getFor(XpComponent.class);

    // ── Events ───────────────────────────────────────────
    public static final ComponentMapper<HitEventComponent> hitEvent = ComponentMapper.getFor(HitEventComponent.class);

}
