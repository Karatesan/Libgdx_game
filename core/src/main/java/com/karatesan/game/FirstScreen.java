package com.karatesan.game;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.combat.*;
import com.karatesan.game.ecs.systems.combat.perks.PiercePerkSystem;
import com.karatesan.game.ecs.systems.combat.perks.RicochetPerkSystem;
import com.karatesan.game.ecs.systems.core.*;
import com.karatesan.game.ecs.systems.economy.MagnetSystem;
import com.karatesan.game.ecs.systems.economy.PerkApplicationSystem;
import com.karatesan.game.ecs.systems.economy.PickupSystem;
import com.karatesan.game.ecs.systems.economy.XpProcessingSystem;
import com.karatesan.game.ecs.systems.movement.EnemySeparationSystem;
import com.karatesan.game.ecs.systems.movement.EnemySystem;
import com.karatesan.game.ecs.systems.movement.MovementSystem;
import com.karatesan.game.ecs.systems.movement.PlayerInputSystem;
import com.karatesan.game.ecs.systems.render.RenderSystem;
import com.karatesan.game.ecs.systems.render.ui.UISystem;
import com.karatesan.game.ecs.systems.waveSystem.WaveSpawnerSystem;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class FirstScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private OrthographicCamera uiCamera;
    private ExtendViewport uiViewport;
    private EntityFactory entityFactory;
    private Stage uiStage;

    // The heart of our new architecture
    private PooledEngine engine;

    public FirstScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        // Our game world is 800x600 units
        viewport = new ExtendViewport(800, 600, camera);

        //UI -------------------------------------------------------
        uiCamera = new OrthographicCamera();
        uiViewport = new ExtendViewport(800, 600, uiCamera);
        // ... your existing camera/viewport setup ...
        uiStage = new Stage(uiViewport, game.spriteBatch);
        // CRITICAL: Route input to the UI Stage
        Gdx.input.setInputProcessor(uiStage);

        //ECS -----------------------------------------------------
        engine = new PooledEngine();
        entityFactory = new EntityFactory(engine);

        entityFactory.createPlayer();
        entityFactory.createSession();

        engine.addSystem(new PlayerInputSystem(camera));
        engine.addSystem(new WeaponSystem(entityFactory));
        engine.addSystem(new InvincibilitySystem());
        engine.addSystem(new EnemySystem());
        engine.addSystem(new EnemySeparationSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new BulletRangeSystem());
        engine.addSystem(new CollisionSystem());
        engine.addSystem(new HitEventProcessingSystem(entityFactory));
        engine.addSystem(new PiercePerkSystem());
        engine.addSystem(new RicochetPerkSystem());
        engine.addSystem(new PostHitBulletSystem());
        engine.addSystem(new CameraSystem(camera));
        engine.addSystem(new MagnetSystem());
        engine.addSystem(new PickupSystem());
        engine.addSystem(new XpProcessingSystem());
        engine.addSystem(new PerkApplicationSystem(game.perkRegistry));
        engine.addSystem(new GameStateSystem());
        engine.addSystem(new RenderSystem(game.spriteBatch, game.shapeDrawer, game.uiFont, camera, game.floorTexture));
        engine.addSystem(
            new UISystem(game.spriteBatch, game.uiFont, game.shapeDrawer, uiViewport, uiStage, game.perkRegistry));
        engine.addSystem(new WaveSpawnerSystem(entityFactory));
        // engine.addSystem(new CooldownSystem());
        engine.addSystem(new DeathSystem(entityFactory));
        engine.addSystem(new LifeTimeSystem());
        engine.addSystem(new CleanupSystem());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        viewport.apply();
        // 3. Tell the batch to use the Camera's coordinates
        game.spriteBatch.setProjectionMatrix(camera.combined);
        engine.update(delta);

        //draw UI on top
        uiViewport.apply();
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        engine.removeAllEntities();
    }

    public enum State {PLAYING, GAME_OVER}
}
