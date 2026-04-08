package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.CharArray;
import com.karatesan.game.ecs.components.UI.FloatingTextComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;
import com.karatesan.game.ecs.components.render.ShapeComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class RenderSystem extends SortedIteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);
    private final ComponentMapper<ShapeComponent> sm = ComponentMapper.getFor(ShapeComponent.class);
    private final ComponentMapper<FloatingTextComponent> txtm = ComponentMapper.getFor(FloatingTextComponent.class);

    private final SpriteBatch batch;
    private final ShapeDrawer shapeDrawer;
    private final BitmapFont bitmapFont;
    private final OrthographicCamera camera;
    private final Texture floorTexture;

    // 1. ADD THIS: The single shared memory buffer for all text rendering
    private final CharArray sharedText = new CharArray();

    private static class ZComparator implements java.util.Comparator<Entity> {
        private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

        @Override
        public int compare(Entity e1, Entity e2) {
            // Get the Z-Index of both entities
            int z1 = tm.get(e1).z;
            int z2 = tm.get(e2).z;

            // Compare them. Lower numbers (0) come first, higher numbers (3) come last.
            return Integer.compare(z1, z2);
        }
    }

    public RenderSystem(SpriteBatch batch, ShapeDrawer shapeDrawer, BitmapFont font, OrthographicCamera camera,
                        Texture floorTexture) {
        // Process anything that has a TransformComponent
        super(Family.all(TransformComponent.class).get(), new ZComparator());
        this.batch = batch;
        this.shapeDrawer = shapeDrawer;
        this.bitmapFont = font;
        this.camera = camera;
        this.floorTexture = floorTexture;
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // --- 1. DRAW THE INFINITE FLOOR ---
        if (floorTexture != null) {
            float viewWidth = camera.viewportWidth * camera.zoom;
            float viewHeight = camera.viewportHeight * camera.zoom;

            float startX = camera.position.x - (viewWidth / 2f);
            float startY = camera.position.y - (viewHeight / 2f);

            // NEW: The Scale Factor!
            // 0.25f means the image will appear 4x smaller on screen.
            // Tweak this number until the tiles look perfect for your game!
            float tileScale = 0.2f;

            // We multiply the texture's actual size by the scale factor
            float scaledWidth = floorTexture.getWidth() * tileScale;
            float scaledHeight = floorTexture.getHeight() * tileScale;

            // Calculate the UV shift using the SCALED size
            float u = startX / scaledWidth;
            float v = startY / scaledHeight;
            float u2 = (startX + viewWidth) / scaledWidth;
            float v2 = (startY + viewHeight) / scaledHeight;

            batch.draw(floorTexture, startX, startY, viewWidth, viewHeight, u, v, u2, v2);
        }

        super.update(deltaTime);

        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);
        ShapeComponent shape = sm.get(entity);
        FloatingTextComponent text = txtm.get(entity);
        if (shape != null) {
            renderGenericShape(entity, shape, transform);
        } else if (text != null) {
            renderFloatingText(text, transform);
        }
    }

    private void renderGenericShape(Entity entity, ShapeComponent shape, TransformComponent transform) {
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
            shapeDrawer.line(startX, startY, endX, endY, 4f);
        }
    }

    private void renderFloatingText(FloatingTextComponent text, TransformComponent transform) {
        // --- ZERO-GC FOUNTAIN TEXT RENDERING ---

        // A. Wipe the shared memory buffer (O(1) operation)
        sharedText.clear();

        // B. Append the raw integer (No Strings created!)
        sharedText.append(text.damageValue);

        // C. Set color and draw directly from the buffer
        bitmapFont.setColor(text.color);
        bitmapFont.getData().setScale(text.scale / 2); // <--- SCALE IT HERE

        bitmapFont.draw(batch, sharedText, transform.x, transform.y);

        // D. Reset color to white so we don't tint the UI
        bitmapFont.setColor(Color.WHITE);
        bitmapFont.getData().setScale(1f); // <--- RESET IT HERE
    }
}
