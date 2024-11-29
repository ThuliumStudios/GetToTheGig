package com.thulium.world;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.thulium.entity.Enemy;
import com.thulium.entity.Entity;
import com.thulium.entity.ParticleEffect;
import com.thulium.game.SpawnProperties;
import com.thulium.item.EquipmentItem;
import com.thulium.item.Item;
import com.thulium.main.MainGame;
import com.thulium.player.Player;
import com.thulium.input.PlayerControllerInput;
import com.thulium.player.PlayerInfo;
import com.thulium.input.PlayerInput;
import com.thulium.player.PlayerProjectile;
import com.thulium.scene.Checkpoint;
import com.thulium.scene.InteractableSceneObject;
import com.thulium.util.MyContactListener;
import com.thulium.util.SpriteAccessor;
import com.thulium.util.Units;

import java.util.Arrays;

public class GameWorld {
	private final Array<Player> players = new Array<>();
	private final Array<Item> items = new Array<>();
	private final Array<InteractableSceneObject> sceneObjects = new Array<>();

	// World environment objects removed here
	private GameWorldEnvironment environment;
	private Vector2 gravity;
	private World world;
	private GameMap map;
	private Player player;
//	private Amp amp;
//	private Cable cable;
	private MyContactListener cl;

	private Box2DDebugRenderer debugRenderer;

	public Array<Enemy> enemies = new Array<>();
	
	// TODO: Delete
	private PlayerInfo info;
	private PlayerInput pIn;
	private Body hitSensor;
	private ShapeRenderer shapeRenderer;

	private TextureAtlas playerAtlas;

	private Array<ParticleEffect> particles;
	private Pool<ParticleEffect> particlePool;

	private TweenManager tweenManager;

	private MainGame game;

	private int flickerHP;

	public GameWorld(MainGame game) {
		this.game = game;
		Box2D.init();
		// Jukebox.playMusic(1);

		// Create Box2d methods and functions
		cl = new MyContactListener(this);
		gravity = new Vector2(0, Units.GRAVITY);
		world = new World(gravity, true);

		BodyDef groundBodyDef = new BodyDef();
		PolygonShape groundBox = new PolygonShape();
		FixtureDef groundDef = new FixtureDef();

		// Implement world functionality methods
		world.setContactListener(cl);
		map = new GameMap(game, game.getBatch());
		map.createBox2dObjects(world, groundBodyDef, groundDef);

		// Create world environment
		environment = new GameWorldEnvironment(map.getProperty("width", Integer.class),
				map.getProperty("height", Integer.class));

		// Create player-related objects
		playerAtlas = game.getAsset("img/player.atlas", TextureAtlas.class);
		player = new Player(playerAtlas);
		flickerHP = player.getHP();
		PlayerProjectile playerProjectile = new PlayerProjectile(game.getAsset("img/axe.atlas", TextureAtlas.class));

		// Populate player-related fields
		cl.setPlayer(player);

		playerProjectile.createBody(world.createBody(playerProjectile.getBodyDef(0, 0)), player.getWidth() / 4f, player.getHeight() / 8f);
		player.setProjectile(playerProjectile);

		// TODO: Delete - creates player collision body
		Preferences prefs = Gdx.app.getPreferences("world");
		addEntity(player, .6f,  .2f, prefs.getFloat("x"), prefs.getFloat("y"), 0,
				player.getHeight() * -.5f + (.2f))
				.setLinearDamping(.5f);
		player.setOriginalMass(5);


		// Generate enemies from map
		Array<SpawnProperties> getEnemies = map.get(SpawnProperties.SpawnType.Enemy);
		for (int i = 0; i < getEnemies.size; i++) {
			SpawnProperties properties = getEnemies.get(i);
			Enemy enemy = new Enemy( game.getAsset("img/" + properties.getName() + ".atlas", TextureAtlas.class), properties);
//			addEntity(enemy, enemy.getWidth() * .3f, enemy.getHeight() * .2f, properties.getX() / 2f,
//					(properties.getY() / 2f) + enemy.getHeight() / 2f, 0, enemy.getHeight() * -.5f);
			addEntity(enemy, enemy.getWidth(), enemy.getHeight(), properties.getX() / 2f,
					(properties.getY() / 2f) + enemy.getHeight() / 2f, 0, enemy.getHeight() * -.5f);
			enemy.setVelocity(-2, 0);
			enemies.add(enemy);
		}

		// Generate items from map
		Array<SpawnProperties> getItems = map.get(SpawnProperties.SpawnType.Item);
		getItems.forEach(spawn -> {
			// TODO: Fix, allow for non-equipment items to spawn
			Item item = new EquipmentItem(game.getAsset("img/items.atlas", TextureAtlas.class)
					.findRegion(spawn.getName()),
					spawn.getX() / 2f, spawn.getY() / 2f, spawn.getWidth(), spawn.getWidth());

			items.add(item);
		});

		// Create scene objects
		Array<SpawnProperties> getScenes = map.get(SpawnProperties.SpawnType.Item);
		getScenes.forEach(spawn -> {
			// TODO: Fix, allow for non-equipment items to spawn
			InteractableSceneObject scene = new Checkpoint(game.getAsset("img/scene.atlas", TextureAtlas.class));
			scene.setBounds(spawn.getX() / 2f, spawn.getY() / 2f, spawn.getWidth(), spawn.getHeight());
			sceneObjects.add(scene);
		});

		// Create transport tiles
		for (int i = 0; i < getScenes.size; i++)
			getScenes.get(i).setCustomData(map.getLayer("bg").getProperties().get("transport" + i, String.class));

		// Test rendering game info
		info = new PlayerInfo(player, game.getAsset("img/hud.atlas", TextureAtlas.class), game.getSkin());

		// Test particle pooling
		particles = new Array<>();
		particlePool = new Pool<>() {
			@Override
			protected ParticleEffect newObject() {
				return new ParticleEffect(game.getAsset("img/particles.atlas", TextureAtlas.class), "cloud");
			}
		};

		groundBox.dispose();

		pIn = new PlayerInput(player);
//		pIn.setAmp(amp);
//		pIn.setCable(cable);

		{	// TODO: Delete. Creates a collision box surrounding the map
			shapeRenderer = new ShapeRenderer();
			int mapWidth = map.getProperty("width", Integer.class);
			int mapHeight = map.getProperty("height", Integer.class);

			BodyDef bodyDef = new BodyDef();
			PolygonShape box = new PolygonShape();
			FixtureDef fixtureDef = new FixtureDef();

			box.setAsBox(mapWidth / 2f, mapHeight / 2f, new Vector2(mapWidth / 2f, mapHeight / 2f), 0);

			ChainShape cs = new ChainShape();
			Vector2[] csPts = new Vector2[5];
			csPts[0] = new Vector2(0, 0);
			csPts[1] = new Vector2(mapWidth, 0);
			csPts[2] = new Vector2(mapWidth, mapHeight);
			csPts[3] = new Vector2(0, mapHeight);
			csPts[4] = new Vector2(0, 0);
			Arrays.asList(csPts).forEach(csp -> csp.scl(.5f));
			cs.createChain(csPts);

			fixtureDef.shape = cs; // box;
			fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
			fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.PLAYER_FLAG;

			bodyDef.type = BodyDef.BodyType.StaticBody;
			bodyDef.position.set(0, 0);

			world.createBody(bodyDef).createFixture(fixtureDef);
		}

		makeHitbox();

		debugRenderer = new Box2DDebugRenderer();
		tweenManager = new TweenManager();
	}

	public void render(Batch batch, float delta) {
		environment.project(batch);
		batch.begin();
		map.renderBG(batch, environment.getCamera());
		batch.end();

		map.render(environment.getCamera(), "bg", "platform");

		environment.project(batch);
		batch.begin();
		enemies.forEach(e -> e.render(batch));
		items.forEach(item -> item.render(batch));
		players.forEach(p -> p.render(batch));
		player.render(batch, environment.getCamera(), delta);
		map.renderFG(batch, environment.getCamera());
		batch.end();

		map.render(environment.getCamera(), "fg");

		environment.project(batch);
		batch.begin();
		map.renderFG(batch, environment.getCamera());
		batch.end();

		batch.begin();
		processParticles(batch);
		batch.end();

		// TODO: Delete
		if (player.isCharging()) {
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.rect(player.getX() + .5f, player.getY() - .25f, player.getWidth() / 2, player.getHeight() / 16f);
			shapeRenderer.setColor(player.getCharge() < 1 ? Color.BLUE : Color.GREEN);
			shapeRenderer.rect(player.getX() + .5f, player.getY() - .25f, (player.getWidth() / 2) * (player.getCharge()), player.getHeight() / 16f);
			shapeRenderer.end();
		}

		info.render(delta);

		if (flickerHP != player.getHP())
			flicker();
		if (player.isDebugging())
			debugRenderer.render(world, environment.getCamera().combined);
	}

	public void flicker() {
		if (flickerHP > player.getHP())
			Timeline.createSequence()
					.push(Tween.to(player, SpriteAccessor.OPACITY, 0).target(0))
					.push(Tween.to(player, SpriteAccessor.OPACITY, .1f).target(1))
					.repeat(5, .1f)
					.start(tweenManager);
		flickerHP = player.getHP();
	}

	public boolean getDebug() {
		return player.isDebugging();
	}

	public void update(float delta) {
		tweenManager.update(delta);

		// TODO: Delete ASAP. Crudely handles player attack "hitbox" sensor
		if (player.isAnimation("attack")) {
			if (player.getCurrentAnimationFrame() == 2) {
				// Set hit sensor location
				hitSensor.setTransform(player.getBody().getPosition().x + (player.getWidth() / 4f) *
								(player.isFlipped() ? -1 : 1),
						player.getY() + 1f, 0);
			} else {
				hitSensor.setTransform(0, -10, 0);
			}
		}

		if (player.isAnimation("run")) {
			if (player.getCurrentAnimationFrame() == 1 || player.getCurrentAnimationFrame() == 4) {
				ParticleEffect p = particlePool.obtain();
				p.initialize("cloud", .5f, .5f, .1f);
				p.setPosition(player.getX() + (player.getWidth() / 2f), player.getY());
				particles.add(p);
			}
		}

		enemies.forEach(e -> {
			e.update(delta);

			if (!e.isAlive()) {
				if (!e.isDestroyed()) {
					System.out.println("Destroying body...");
					e.destroyBody();
					Gdx.app.postRunnable(() -> world.destroyBody(e.getBody()));
					System.out.println("Body destroyed.");
				}

				if (e.isAnimation("death") && e.isAnimationFinished())
					enemies.removeValue(e, true);
			}
		});

		items.forEach(item -> {
			// TODO: Fix ASAP - not all items will have the same logic. Added for convenience in testing
			if (player.collidesWith(item.getSprite())) {
				item.obtain();
				items.removeValue(item, true);
				playerAtlas = game.getAsset("img/playerwaxe.atlas", TextureAtlas.class);
				player.switchStates(playerAtlas);
			}
		});

		info.update(delta);
		info.setDebug(player.isDebugging());
		player.setOnGround(cl.isOnGround());
		player.update(Gdx.graphics.getDeltaTime());

		environment.updateCamera(player.getBody().getPosition(), delta);

//		if (player.isPullingAmp() || amp.isPullingPlayer()) {
//			if (cable.getJoint().getMaxLength() > 0) {
//				pullAmp(delta);
//			}
//		}
//
//		// TODO: Delet dis
//		if (!cable.isConnected() && cable.getState() == 0)
//			cutCable();
//		else if (cable.getState() == 1 && cable.isConnected()) {
//			cable.setState(0);
//			cable.setJoint(world.createJoint(cable.getBodyDef(amp.getBody(), player.getBody())));
//		}

		// TODO: Change collision filters for all entities in loop
		if (player.getBody().getLinearVelocity().y >= .001f) {
			player.changeCollisionFilters(Units.PLAYER_FLAG, (short) 0);
		} else {
			player.changeCollisionFilters(Units.PLAYER_FLAG, (short) (Units.GROUND_FLAG | Units.ENTITY_FLAG));
		}



//		if (!amp.isStateLocked()) {
//			if (amp.getBody().getLinearVelocity().y >= .001f) {
//				amp.changeCollisionFilters(Units.ENTITY_FLAG, (short)0);
//				// player.changeCollisionGroup((short) 2);
//			} else {
//				amp.changeCollisionFilters(Units.ENTITY_FLAG, Units.GROUND_FLAG);
//				// player.changeCollisionGroup((short) 1);
//			}
//		}

		player.changeCollisionGroup(player.getBody().getLinearVelocity().y >= .001f
				// || player.getBody().getPosition().y < amp.getBody().getPosition().y + .6f
				//? (short) 2 : (short) 1
				? Units.PLAYER_FLAG : Units.ALL_FLAG
		);

		// TODO: Delete. Handles player death/dying, needs to be moved
		if (player.getHP() == 0) {
			info.setStatus("Press R to respawn");
			player.die();
			player.setXVelocity(0);
			player.setVelocity(0, 0);
			if (Gdx.input.isKeyJustPressed(Keys.R))
				respawn();
		}

		world.step(Math.min(1 / 60f, delta), 6, 2);
	}

	public Player getPlayer() {
		return player;
	}

	public Body addEntity(Entity e, float width, float height, float spawnX, float spawnY, float x, float y) {
		return e.createBody(world.createBody(e.getBodyDef(spawnX, spawnY)), width / 2f, height / 2f, x, y);
	}

//	public float cameraScale(boolean width) {
//		return width ? (textCamera.viewportWidth / camera.viewportWidth)
//				: (textCamera.viewportHeight / camera.viewportHeight);
//	}

//	public void pullAmp(float delta) {
//		// amp.changeCollisionFilters(Units.ENTITY_FLAG, Units.ALL_FLAG);
//		cable.getJoint().setMaxLength(cable.getJoint().getMaxLength() - (delta * 2f));
//	}
//
//	public void cutCable() {
//		world.destroyJoint(cable.getJoint());
//		cable.setState(1);
//	}

	// TODO: Delete
	public void respawn() {
		player.setHP(4);
		// player.getBody().setTransform(new Vector2(2.5f, 2.5f), 0); // TODO: Fix ASAP
		player.setVelocity(0, 0);
		info.setStatus("");
		flicker();
	}

	public void hitPlayer() {
		if (environment.shake()) {
			// Create particle effect
			ParticleEffect p = particlePool.obtain();
			p.initialize("hit0", 1, 1, .5f);
			p.setPosition(player.getX() + (player.isFlipped() ? 0 : player.getWidth() / 2f), player.getY() + .5f);
			particles.add(p);
		}
	}

	public void resize(int width, int height) {
		info.resize(width, height);
		environment.resize(width, height);
	}

	public void dispose() {
		// Confirmed this is getting called
		game.saveValue("x", player.getBody().getPosition().x, MainGame.Prefs.world);
		game.saveValue("y", player.getBody().getPosition().y, MainGame.Prefs.world);
		players.forEach(Player::dispose);
		info.dispose();
		world.dispose();
		player.dispose();
		map.dispose();
		shapeRenderer.dispose();
		debugRenderer.dispose();
		playerAtlas.dispose();
		particlePool.clear();
	}

	// Add particle to pool
	public void processParticles(Batch batch) {
		particles.forEach(p -> {
			p.draw(batch);
			if (p.isFinished()) {
				particlePool.free(p);
				particles.removeValue(p, false);
			}
		});
	}

	public void generateEntities() {

	}

	public void generateScene() {

	}

	public void generateItems() {

	}

	public InputProcessor getInputProcessors() {
		InputMultiplexer input = new InputMultiplexer(pIn, info.getStage());
		Controllers.addListener(new PlayerControllerInput(pIn, this));
		return input;
	}

	// TODO: DELETE everything below
	public void makeHitbox() {
		PolygonShape hitboxShape = new PolygonShape();
		BodyDef hitDef = new BodyDef();
		FixtureDef hitboxDef = new FixtureDef();

		// hitboxShape.setAsBox(.5f, .5f, new Vector2(0, 0), 0);
		hitboxShape.setAsBox(.5f, 1, new Vector2(0, 0), 0);

		hitboxDef.shape = hitboxShape;
		hitboxDef.filter.categoryBits = Units.PLAYER_FLAG;
		hitboxDef.filter.maskBits = Units.ENTITY_FLAG;
		hitboxDef.isSensor = true;

		hitDef.type = BodyDef.BodyType.DynamicBody;
		hitDef.position.set(0, 0);
		hitDef.fixedRotation = true;
		hitDef.gravityScale = 0;

		hitSensor = world.createBody(hitDef);
		hitSensor.createFixture(hitboxDef).setUserData("hit"); // "hit"

		hitboxShape.dispose();
	}
}
