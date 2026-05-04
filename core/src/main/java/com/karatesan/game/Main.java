package com.karatesan.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.karatesan.game.data.registry.PerkRegistry;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {

    SpriteBatch spriteBatch;
    TextureRegion textureRegion;
    ShapeDrawer shapeDrawer;

    public BitmapFont uiFont;
    public Texture floorTexture;

    PerkRegistry perkRegistry;


    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);

        Texture texture = new Texture(pixmap);
        this.textureRegion = new TextureRegion(texture);
        pixmap.dispose();
        this.shapeDrawer = new ShapeDrawer(spriteBatch, textureRegion);

        //Ground
        floorTexture = new Texture(Gdx.files.internal("ui/Floor.jpg"));
        floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        //Font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Font.ttf"));

        // 2. Set the parameters (Size, Color, etc.)
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48; // 24 pixels tall
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2f; // A nice black outline makes text readable on any background!
        parameter.borderColor = Color.BLACK;

        // 3. Generate the font and dispose of the generator (saves RAM)
        this.uiFont = generator.generateFont(parameter);

        this.uiFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // NEW: Tell the font to draw itself at 33% scale to fit our 800x600 world!
        //this.uiFont.getData().setScale(0.33f);

        generator.dispose();

        perkRegistry = new PerkRegistry(Gdx.files.internal("data/perks2.json"));


        setScreen(new FirstScreen(this));
    }

    @Override
    public void dispose() {
        if (screen != null) screen.hide();
        spriteBatch.dispose();
        textureRegion.getTexture().dispose();
        uiFont.dispose();
        floorTexture.dispose();
        super.dispose();
    }

    @Override
    public void render() {
        super.render();
    }
}
