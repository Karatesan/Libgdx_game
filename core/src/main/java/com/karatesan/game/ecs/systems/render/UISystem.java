package com.karatesan.game.ecs.systems.render;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.karatesan.game.ecs.components.combat.HealthComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.perks.PerkChoiceComponent;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.utility.ECSUtils;
import com.karatesan.game.ecs.utility.State;
import com.karatesan.game.perks.PerkOffer;
import com.karatesan.game.perks.PerkRegistry;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UISystem extends IteratingSystem {

    private final ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeDrawer shapeDrawer;
    private final Viewport viewport;
    private Entity sessionEntity;
    private final CharArray UIText = new CharArray();

    private final Stage stage;
    private final PerkRegistry perkRegistry;
    private boolean perkUIBuilt = false;

    private final Drawable cardBg;
    private final Drawable cardHoverBg;

    private static final float CARD_WIDTH = 220f;
    private static final float CARD_PAD = 12f;
    private static final float PERK_FONT_SCALE = 0.35f;

    public UISystem(SpriteBatch batch, BitmapFont font, ShapeDrawer shapeDrawer, Viewport viewport, Stage stage,
                    PerkRegistry perkRegistry) {
        super(Family.all(PlayerComponent.class, HealthComponent.class).get());
        this.batch = batch;
        this.shapeDrawer = shapeDrawer;
        this.font = font;
        this.viewport = viewport;
        this.stage = stage;
        this.perkRegistry = perkRegistry;

        TextureRegionDrawable base = new TextureRegionDrawable(shapeDrawer.getRegion());
        cardBg = base.tint(new Color(0.15f, 0.15f, 0.2f, 0.95f));
        cardHoverBg = base.tint(new Color(0.25f, 0.25f, 0.35f, 0.95f));
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    public void update(float deltaTime) {
        // 1. Get the dynamic width and height of the screen
        float screenW = viewport.getWorldWidth();
        float screenH = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        super.update(deltaTime); // Draws Player HP

        SessionComponent session = sm.get(sessionEntity);

        // --- DRAW KILL COUNT (Anchored to Top-Right) ---
        drawKillCounter(session, screenW, screenH);

        // --- DRAW CLOCK (Top Center) ZERO GC ---
        drawTimer(session, screenW, screenH);

        // --- DRAW WAVE ANNOUNCEMENT (Anchored to Dead Center) ---
        if (session.waveTextTimer > 0) {
            drawWaveAnnoucement(session, screenW, screenH);
        }

        // --- DRAW GAME OVER ---
        if (session.currentState == State.GAME_OVER) {
            drawGameOver(screenW, screenH);
        }

        // --- DRAW XP BAR (Anchored to Top-Center, just below the clock) ---
        drawXpBar(screenW, screenH, session);

        batch.end();

        if (session.currentState == State.LEVEL_UP) {
            drawLevelUpUI(deltaTime, screenW, screenH);
        } else if (perkUIBuilt) {
            // Left LEVEL_UP state — clean up
            stage.clear();
            perkUIBuilt = false;
        }
    }

    private void drawLevelUpUI(float deltaTime, float screenW, float screenH) {
        if (!perkUIBuilt) {
            buildPerkUI();
            perkUIBuilt = true;
        }

        // Dim the game behind the cards
        batch.begin();
        shapeDrawer.setColor(0, 0, 0, 0.7f);
        shapeDrawer.filledRectangle(0, 0, screenW, screenH);
        batch.end();

        // Draw interactive cards
        float savedScale = font.getData().scaleX;
        font.getData().setScale(PERK_FONT_SCALE);
        stage.act(deltaTime);
        stage.draw();
        font.getData().setScale(savedScale);
    }

    private void buildPerkUI() {
        stage.clear();

        Entity player = ECSUtils.getPlayer(getEngine());
        PerkInventoryComponent inventory = player.getComponent(PerkInventoryComponent.class);
        Array<PerkOffer> offers = perkRegistry.generateOffers(inventory, 3);

        // Edge case: all perks maxed
        if (offers.size == 0) {
            player.remove(LevelUpComponent.class);
            perkUIBuilt = false;
            return;
        }

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Title
        Label title = new Label("CHOOSE A PERK", new Label.LabelStyle(font, Color.YELLOW));
        root.add(title).colspan(offers.size).padBottom(30f);
        root.row();

        // Cards
        for (int i = 0; i < offers.size; i++) {
            root.add(buildCard(offers.get(i))).width(CARD_WIDTH).pad(10f);
        }

        stage.addActor(root);
    }

    private Table buildCard(final PerkOffer offer) {
        final Table card = new Table();
        card.setBackground(cardBg);
        card.pad(CARD_PAD);
        card.defaults().width(CARD_WIDTH - CARD_PAD * 2);
        card.setTouchable(Touchable.enabled);

        // --- Perk Name ---
        Label name = new Label(offer.definition.name, new Label.LabelStyle(font, Color.WHITE));
        name.setAlignment(Align.center);
        card.add(name).padBottom(8f);
        card.row();

        // --- Flavor Description ---
        Label desc = new Label(offer.definition.description, new Label.LabelStyle(font, Color.LIGHT_GRAY));
        desc.setWrap(true);
        desc.setAlignment(Align.center);
        card.add(desc).padBottom(12f);
        card.row();

        // --- Level Indicator ---
        String levelText = "Level " + offer.nextLevel + " / " + offer.definition.maxLevel;
        Label level = new Label(levelText, new Label.LabelStyle(font, Color.CYAN));
        level.setAlignment(Align.center);
        card.add(level).padBottom(8f);
        card.row();

        // --- Separator ---
        Image separator = new Image(new TextureRegionDrawable(shapeDrawer.getRegion()));
        separator.setColor(Color.GRAY);
        card.add(separator).height(1f).fillX().padBottom(8f);
        card.row();

        // --- What This Level Does ---
        Label effect = new Label(offer.levelData.levelUpDescription, new Label.LabelStyle(font, Color.GREEN));
        effect.setWrap(true);
        effect.setAlignment(Align.center);
        card.add(effect).expandY().top();

        // --- Interaction ---
        card.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                card.setBackground(cardHoverBg);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                card.setBackground(cardBg);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPerkSelected(offer);
            }
        });

        return card;
    }

    // =========================================================================
    // PERK SELECTION CALLBACK
    // =========================================================================

    private void onPerkSelected(PerkOffer offer) {
        Entity player = ECSUtils.getPlayer(getEngine());

        PerkChoiceComponent choice = getEngine().createComponent(PerkChoiceComponent.class);
        choice.perkId = offer.definition.id;
        player.add(choice);
        stage.clear();
        perkUIBuilt = false;
    }

    private void drawXpBar(float screenW, float screenH, SessionComponent session) {
        float barWidth = screenW;
        float barHeight = 5f;
        // Center the bar horizontally
        float barX = 0;
        // Place it 50 pixels down from the top edge
        float barY = screenH - barHeight;

        float fillRatio = session.currentXp / session.xpToNextLevel;

        shapeDrawer.setColor(Color.DARK_GRAY);
        shapeDrawer.filledRectangle(barX, barY, barWidth, barHeight);
        shapeDrawer.setColor(Color.CYAN);
        shapeDrawer.filledRectangle(barX, barY, barWidth * fillRatio, barHeight);
    }

    private void drawGameOver(float screenW, float screenH) {
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", (screenW / 2f) - 60f, (screenH / 2f) + 50f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Press 'R' to Restart", (screenW / 2f) - 160f, (screenH / 2f));
    }

    private void drawWaveAnnoucement(SessionComponent session, float screenW, float screenH) {
        UIText.clear();
        UIText.append("WAVE ").append(session.currentWave);

        font.setColor(Color.YELLOW);
        // Center it perfectly in the middle of the screen
        font.draw(batch, UIText, (screenW / 2f) - 60f, (screenH / 2f) + 50f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1f);
    }

    private void drawTimer(SessionComponent session, float screenW, float screenH) {
        int totalSeconds = (int) session.timeSurvived;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        UIText.clear();
        UIText.append(minutes).append(":");
        if (seconds < 10) UIText.append("0"); // Add leading zero (e.g., 1:05)
        UIText.append(seconds);
        font.getData().setScale(.5f); // Make it big!

        // screenW / 2 finds the exact dead center of the screen
        font.draw(batch, UIText, (screenW / 2f) - 20f, screenH - 20f);
        font.getData().setScale(1f); // Make it big!
    }

    private void drawKillCounter(SessionComponent session, float screenW, float screenH) {
        UIText.clear();
        UIText.append("Killed: ").append(session.kilLCount);
        font.getData().setScale(.5f);
        // screenW - 150 pixels from the right edge, screenH - 20 pixels from the top
        font.draw(batch, UIText, screenW - 150f, screenH - 20f);
        font.getData().setScale(1f);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        HealthComponent health = hm.get(player);
        float screenH = viewport.getWorldHeight();

        UIText.clear();
        UIText.append("HP: ").append((int) health.currentHp).append("/").append((int) health.maxHp);
        font.getData().setScale(.5f);
        // Anchored to Top-Left (20 pixels from left edge, 20 pixels from top edge)
        font.draw(batch, UIText, 20f, screenH - 20f);
        font.getData().setScale(1f);

    }
}
