package com.karatesan.game.ecs.systems.render.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.perks.PerkOffer;
import com.karatesan.game.perks.PerkRegistry;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class LevelUpUIBuilder {

    private final BitmapFont font;
    private final ShapeDrawer shapeDrawer;

    private final Stage stage;
    private final PerkRegistry perkRegistry;

    private final Drawable cardBg;
    private final Drawable cardHoverBg;

    private static final float CARD_WIDTH = 220f;
    private static final float CARD_PAD = 12f;

    public LevelUpUIBuilder(BitmapFont font, ShapeDrawer shapeDrawer, Stage stage, PerkRegistry perkRegistry) {
        this.font = font;
        this.shapeDrawer = shapeDrawer;
        this.stage = stage;
        this.perkRegistry = perkRegistry;

        TextureRegionDrawable base = new TextureRegionDrawable(shapeDrawer.getRegion());
        cardBg = base.tint(new Color(0.15f, 0.15f, 0.2f, 0.95f));
        cardHoverBg = base.tint(new Color(0.25f, 0.25f, 0.35f, 0.95f));
    }


    public boolean buildPerkUI(float luck, PerkInventoryComponent inventory, PerkSelectionListener callback) {
        stage.clear();

        Array<PerkOffer> offers = perkRegistry.generateOffers(inventory, 3, luck);

        // Edge case: all perks maxed
        if (offers.size == 0) {
            return false;
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
            root.add(buildCard(offers.get(i), callback)).width(CARD_WIDTH).pad(10f);
        }

        stage.addActor(root);
        return true;
    }

    private Table buildCard(final PerkOffer offer, PerkSelectionListener callback) {
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
                callback.perkSelectionCallback(offer);
            }
        });

        return card;
    }


}
