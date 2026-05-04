package com.karatesan.game;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.karatesan.game.data.registry.BlueprintRegistry;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.combat.*;
import com.karatesan.game.ecs.systems.perks.HpRegenSystem;
import com.karatesan.game.ecs.systems.perks.PiercePerkSystem;
import com.karatesan.game.ecs.systems.perks.RicochetPerkSystem;
import com.karatesan.game.ecs.systems.core.*;
import com.karatesan.game.ecs.systems.debug.DebugDisplaySystem;
import com.karatesan.game.ecs.systems.economy.*;
import com.karatesan.game.ecs.systems.movement.EnemySeparationSystem;
import com.karatesan.game.ecs.systems.movement.EnemySystem;
import com.karatesan.game.ecs.systems.movement.MovementSystem;
import com.karatesan.game.ecs.systems.movement.PlayerInputSystem;
import com.karatesan.game.ecs.systems.render.RenderSystem;
import com.karatesan.game.ecs.systems.render.UISystem;
import com.karatesan.game.ecs.systems.spawning.WaveSpawnerSystem;

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
    private BlueprintRegistry blueprintRegistry;
    private Stage uiStage;
    GameContext context;
    GameConfig config;

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

        config = GameConfig.defaults();

        context = new GameContext();
        context.registerListeners(engine);

        blueprintRegistry = new BlueprintRegistry();
        blueprintRegistry.load();

        entityFactory = new EntityFactory(engine, config, blueprintRegistry);

        entityFactory.createPlayer(config.viewportWidth / 2, config.viewportHeight / 2);
        entityFactory.createSession();

        engine.addSystem(new PlayerInputSystem(camera, context));
        engine.addSystem(new HpRegenSystem());
        engine.addSystem(new WeaponSystem(entityFactory, context));
        engine.addSystem(new InvincibilitySystem());
        engine.addSystem(new EnemySystem(context));
        engine.addSystem(new EnemySeparationSystem(config));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new BulletRangeSystem());
        engine.addSystem(new CollisionSystem(context, config));
        engine.addSystem(new DefenseSystem());
        engine.addSystem(new HitEventProcessingSystem(entityFactory));
        engine.addSystem(new PiercePerkSystem());
        engine.addSystem(new RicochetPerkSystem(config));
        engine.addSystem(new PostHitBulletSystem());
        engine.addSystem(new CameraSystem(camera, context));
        engine.addSystem(new MagnetSystem(context, config));
        engine.addSystem(new PickupSystem(context));
        engine.addSystem(new XpProcessingSystem(context));    // priority 5
        engine.addSystem(new LevelUpSystem(context, config));
        engine.addSystem(new GameStateSystem(context, config));                          // 0
        engine.addSystem(new PerkApplicationSystem(game.perkRegistry, context));              // 1
        engine.addSystem(new StatRecalculationSystem(context, config, blueprintRegistry, game.perkRegistry));
        engine.addSystem(new SessionTimerSystem(config, context));
        engine.addSystem(new RenderSystem(game.spriteBatch, game.shapeDrawer, game.uiFont, camera, game.floorTexture));
        engine.addSystem(
            new UISystem(game.spriteBatch, game.uiFont, game.shapeDrawer, uiViewport, uiStage, game.perkRegistry));
        engine.addSystem(new WaveSpawnerSystem(entityFactory, context, config));
        // engine.addSystem(new CooldownSystem());
        engine.addSystem(new DeathSystem(entityFactory, context));
        engine.addSystem(new LifeTimeSystem());
        engine.addSystem(new CleanupSystem());
        engine.addSystem(new DebugDisplaySystem(game.spriteBatch, context, config));
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
