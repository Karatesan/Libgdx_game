package com.karatesan.game.ecs.systems.render.ui;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.combat.StatsComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.perks.PerkChoiceComponent;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.utility.State;
import com.karatesan.game.perks.PerkOffer;
import com.karatesan.game.perks.PerkRegistry;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UISystem extends EntitySystem {

    private final ComponentMapper<HealthComponent> hpMapper = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<SessionComponent> sessionMapper = ComponentMapper.getFor(SessionComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<PerkInventoryComponent> perkMapper = ComponentMapper.getFor(
        PerkInventoryComponent.class);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final HudRenderer hudRenderer; //
    private final LevelUpController levelUpController;
    private ImmutableArray<Entity> playerEntities;
    private final PerkSelectionListener perkSelectionCallback;

    private final Viewport viewport;
    private Entity sessionEntity;

    private static final float PERK_FONT_SCALE = 0.35f;


    public UISystem(SpriteBatch batch, BitmapFont font, ShapeDrawer shapeDrawer, Viewport viewport, Stage stage,
                    PerkRegistry perkRegistry) {
        perkSelectionCallback = this::onPerkSelected;
        this.hudRenderer = new HudRenderer(batch, font, shapeDrawer);
        this.levelUpController = new LevelUpController(
            new LevelUpUIBuilder(font, shapeDrawer, stage, perkRegistry, perkSelectionCallback), stage,
            perkSelectionCallback);
        this.batch = batch;
        this.viewport = viewport;
        this.font = font;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
        playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class, HealthComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {

        // 1. Get the dynamic width and height of the screen
        float screenW = viewport.getWorldWidth();
        float screenH = viewport.getWorldHeight();

        if (playerEntities.size() == 0) return;
        Entity player = playerEntities.first();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.setColor(Color.WHITE); // guarantee clean color state each frame
        batch.begin();

        //Draw hp -----------------------------------------------------------
        HealthComponent health = hpMapper.get(player);
        hudRenderer.drawHP(health.currentHp, health.maxHp, screenW, screenH);

        SessionComponent session = sessionMapper.get(sessionEntity);

        // --- DRAW KILL COUNT (Anchored to Top-Right) ---
        hudRenderer.drawKillCounter(session.kilLCount, screenW, screenH);

        // --- DRAW CLOCK (Top Center) ZERO GC ---
        hudRenderer.drawTimer(session.timeSurvived, screenW, screenH);

        // --- DRAW WAVE ANNOUNCEMENT (Anchored to Dead Center) ---
        if (session.waveTextTimer > 0) {
            hudRenderer.drawWaveAnnouncement(session.currentWave, screenW, screenH);
        }

        // --- DRAW GAME OVER ---
        if (session.currentState == State.GAME_OVER) {
            hudRenderer.drawGameOver(screenW, screenH);
        }

        // --- DRAW XP BAR (Anchored to Top-Center, just below the clock) ---
        hudRenderer.drawXpBar(session.currentXp, session.xpToNextLevel, screenW, screenH);

        batch.end();

        PerkInventoryComponent inventory = perkMapper.get(player);
        StatsComponent statsComponent = statsMapper.get(player);

        boolean isLevelUp = session.currentState == State.LEVEL_UP;
        if (isLevelUp) {
            hudRenderer.dimBackground(screenW, screenH);
            font.getData().setScale(PERK_FONT_SCALE);
        }
        levelUpController.update(session.currentState, inventory, statsComponent.luck, deltaTime);
        if (isLevelUp) {
            font.getData().setScale(1f);
        }
    }


    // =========================================================================
    // PERK SELECTION CALLBACK
    // =========================================================================

    private void onPerkSelected(PerkOffer offer) {
        PerkChoiceComponent choice = getEngine().createComponent(PerkChoiceComponent.class);
        choice.perkId = offer.definition.id;
        playerEntities.first().add(choice);
        levelUpController.reset();
    }
}
