package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.components.UI.FloatingTextComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class RenderSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);
    private final ComponentMapper<ShapeComponent> sm = ComponentMapper.getFor(ShapeComponent.class);
    private final ComponentMapper<FloatingTextComponent> txtm = ComponentMapper.getFor(FloatingTextComponent.class);

    private final SpriteBatch batch;
    private final ShapeDrawer shapeDrawer;
    private final BitmapFont bitmapFont;
    private final OrthographicCamera camera;

    public RenderSystem(SpriteBatch batch, ShapeDrawer shapeDrawer, BitmapFont font, OrthographicCamera camera) {
        // Process anything that has a TransformComponent
        super(Family.all(TransformComponent.class).get());
        this.batch = batch;
        this.shapeDrawer = shapeDrawer;
        this.bitmapFont = font;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        // This runs ONCE per frame before iterating through entities
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // This triggers processEntity() for every valid entity
        super.update(deltaTime);

        // This runs ONCE per frame after all entities are drawn
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        ShapeComponent shape = sm.get(entity);
        FloatingTextComponent text = txtm.get(entity);

        if (shape != null) {

            // 1. Draw the base shape for EVERY entity (Player, Bullet, Enemy)
            shapeDrawer.setColor(shape.color);
            shapeDrawer.filledCircle(transform.x, transform.y, transform.size / 2);

            // 2. If it happens to be the player, draw the gun barrel on top
            if (pm.has(entity)) {
                float startX = transform.x + MathUtils.cosDeg(transform.rotation) * transform.size / 2;
                float startY = transform.y + MathUtils.sinDeg(transform.rotation) * transform.size / 2;
                float endX = startX + MathUtils.cosDeg(transform.rotation) * transform.size / 2;
                float endY = startY + MathUtils.sinDeg(transform.rotation) * transform.size / 2;

                // 3. Draw the gun barrel (a thick white line)
                shapeDrawer.setColor(Color.WHITE);
                shapeDrawer.line(startX, startY, endX, endY, 4f); // 4f is the line thickness
            }
        } else if (text != null) {

            bitmapFont.setColor(text.color);
            bitmapFont.draw(batch, text.text, transform.x, transform.y);
            //reset color to white if we draw red crit - because it woudl cause ui to draw red
            bitmapFont.setColor(Color.WHITE);
        }
    }
}

