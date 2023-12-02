package com.thulium.world;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thulium.entity.Amp;
import com.thulium.entity.Cable;
import com.thulium.entity.Enemy;
import com.thulium.entity.Entity;
import com.thulium.entity.ParticleEffect;
import com.thulium.game.PauseMenu;
import com.thulium.game.SpawnProperties;
import com.thulium.main.MainGame;
import com.thulium.player.Player;
import com.thulium.player.PlayerControllerInput;
import com.thulium.player.PlayerInfo;
import com.thulium.player.PlayerInput;
import com.thulium.util.Jukebox;
import com.thulium.util.MyContactListener;
import com.thulium.util.SpriteAccessor;
import com.thulium.util.Units;

import java.util.Arrays;
import java.util.stream.IntStream;

public class GameWorld {
	private Array<Player> players = new Array<>();
	private OrthographicCamera camera;
	private OrthographicCamera textCamera;
	private Viewport viewport;
	private Vector2 gravity;
	private Vector2 spawn;
	private World world;
	private GameMap map;
	private Player player;
	private Amp amp;
	private Cable cable;
	private MyContactListener cl;

	private Box2DDebugRenderer debugRenderer;

	public Array<Enemy> enemies = new Array<>();
	
	// TODO: Delete
	private PlayerInfo info;
	private PlayerInput pIn;
	private CameraHelper shaker;
	private Body hitSensor;

	private TextureAtlas playerAtlas;

	private InputMultiplexer input;

	private Array<ParticleEffect> particles;
	private Pool<ParticleEffect> particlePool;

	private Jukebox jukebox;
	private PauseMenu pause;
	private TweenManager tweenManager;

	private int flickerHP;

	public GameWorld(MainGame game) {
		Box2D.init();

		jukebox = new Jukebox();
		// jukebox.playMusic(1);

		pause = new PauseMenu(game.getSkin());

		viewport = new StretchViewport(Units.WIDTH, Units.HEIGHT);//StretchViewport(Units.WIDTH, Units.HEIGHT);
		camera = new OrthographicCamera();
		viewport.setCamera(camera);
		viewport.apply();
		camera.update();

		textCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cl = new MyContactListener();

		gravity = new Vector2(0, -9.81f * 3);
		world = new World(gravity, true);
		world.setContactListener(cl);

		BodyDef groundBodyDef = new BodyDef();
		PolygonShape groundBox = new PolygonShape();
		FixtureDef groundDef = new FixtureDef();

		map = new GameMap(game, game.getBatch());
		map.createBox2dObjects(world, groundBodyDef, groundDef);

		spawn = new Vector2(2.5f, 2.5f);

		playerAtlas = new TextureAtlas(Gdx.files.internal("img/player.atlas"));
		player = new Player(playerAtlas);
		flickerHP = player.getHP();

		cl.setPlayer(player);

		// TODO: Delete - creates player collision body
		addEntity(player, .6f,  .2f, spawn.x, spawn.y, 0,
				player.getHeight() * -.5f + (.2f))
				.setLinearDamping(.5f);
		player.setOriginalMass(5);


		// Testing 1/4 cable 
		amp = new Amp(playerAtlas.findRegion("amp"));
//		amp.createBody(world.createBody(amp.getBodyDef(spawn.x + 2, spawn.y)), "amp", .4f, .4f, true);
//		amp.setOriginalMass(1.1f);
		cable = new Cable();
//		cable.setJoint(world.createJoint(cable.getBodyDef(amp.getBody(), player.getBody())));

		// Generate enemies from map
		for (int i = 0; i < map.getEnemies().size; i++) {
			SpawnProperties properties = map.getEnemies().get(i);
			Enemy enemy = new Enemy( game.getAsset("img/" + properties.getName() + ".atlas", TextureAtlas.class), properties);
			addEntity(enemy, enemy.getWidth() * .3f, enemy.getHeight() * .2f, properties.getX() / 2f,
					(properties.getY() / 2f) + enemy.getHeight() / 2f, 0, enemy.getHeight() * -.5f);
			// enemy.changeCollisionGroup((short) 3);
			enemy.setVelocity(-2, 0);
			enemies.add(enemy);
		}
		
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

		pIn = new PlayerInput(player, camera);
		pIn.setAmp(amp);
		pIn.setCable(cable);

		{	// TODO: Delete
			int mapWidth = map.getProperty("width", Integer.class);
			int mapHeight = map.getProperty("height", Integer.class);
			shaker = new CameraHelper();//(camera, 3, 1, .9f);

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

			fixtureDef.shape = cs; //box;
			fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
			fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.PLAYER_FLAG;

			bodyDef.type = BodyDef.BodyType.StaticBody;
			bodyDef.position.set(0, 0);

			world.createBody(bodyDef).createFixture(fixtureDef);
		}

		makeHitbox();

		debugRenderer = new Box2DDebugRenderer();

		input = new InputMultiplexer(pIn, info.getStage());
		Gdx.input.setInputProcessor(input);
		Controllers.addListener(new PlayerControllerInput(pIn, this));

		tweenManager = new TweenManager();
	}

	public void render(Batch batch, float delta) {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		map.renderBG(batch, camera);
		batch.end();

		map.render(camera, "bg", "platforms");

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		enemies.forEach(e -> e.render(batch));
		players.forEach(p -> p.render(batch));
//		amp.render(batch);
		player.render(batch);

		map.renderFG(batch, camera);
		batch.end();

//		Array<Joint> joints = new Array<>();
//		world.getJoints(joints);
//		joints.forEach(j -> {
//			shapes.rectLine(j.getAnchorA(), j.getAnchorB(), .025f);
//		});

		map.render(camera, "fg");

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		map.renderFG(batch, camera);
		batch.end();

		batch.begin();
		processParticles(batch);
		batch.end();

		info.render(delta);
		if (flickerHP != player.getHP())
			flicker();
		
		if (player.isDebugging()) {
			debugRenderer.render(world, camera.combined);
		}
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

	public void update(float delta) {
		tweenManager.update(delta);

		// TODO: Delete ASAP
		if (player.isAnimation("attack")) {
			if (player.getCurrentAnimationFrame() == 2) {
				if (!shaker.isShaking()) {
					// Camera shake
					shaker.shake(.2f, .05f);

					// Create particle effect
					ParticleEffect p = particlePool.obtain();
					p.initialize("hit0", 1, 1, .5f);
					p.setPosition(player.getX() + (player.isFlipped() ? 0 : player.getWidth() / 2f), player.getY() + .5f);
					particles.add(p);
				}
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

		// TODO: Consider moving this to a player state
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			pause.show();
		if (pause.isShowing())
			return;


		// TODO: Don't do this
		float camX = MathUtils.clamp(player.getBody().getPosition().x, camera.viewportWidth / 2f,
				map.getProperty("width", Integer.class)/2f - camera.viewportWidth/2f);
		float camY = MathUtils.clamp(player.getBody().getPosition().y, camera.viewportHeight / 2f,
				map.getProperty("height", Integer.class));
		if (shaker.isShaking()) {
			shaker.update(camera, camX, camY, delta);
		} else {
			camera.position.set(camX, camY, 0);
		}

		// camera.position.set(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
		textCamera.position.set(camera.position.x * (cameraScale(true)), camera.position.y * (cameraScale(false)), 0);

		enemies.forEach(e -> {
			e.update(delta);

			if (!e.isAlive()) {
				if (!e.isDestroyed()) {
					System.out.println("Destroying body...");
					e.destroyBody();
					Gdx.app.postRunnable(() -> world.destroyBody(e.getBody()));
					System.out.println("Body destroyed.");
				}

				if (e.isAnimation("death") && e.isAnmationFinished())
					enemies.removeValue(e, true);
			}
		});




		info.update(delta);
		info.setDebug(player.isDebugging());
		player.setOnGround(cl.isOnGround());
		player.update(Gdx.graphics.getDeltaTime());

		textCamera.update();
		camera.update();

		// jukebox.setVolume(player.isPaused() ? 0 : 1);

		if (player.isPullingAmp() || amp.isPullingPlayer()) {
			if (cable.getJoint().getMaxLength() > 0) {
				pullAmp(delta);
			}
		}

		// TODO: Delet dis -.-
		if (!cable.isConnected() && cable.getState() == 0)
			cutCable();
		else if (cable.getState() == 1 && cable.isConnected()) {
			cable.setState(0);
			cable.setJoint(world.createJoint(cable.getBodyDef(amp.getBody(), player.getBody())));
		}

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
				? (short) 2 : (short) 1);

		// TODO: Delete. Handles player death/dying
		if (player.getHP() == 0) {
			input.clear();
			info.setStatus("Press R to respawn");
			player.setXVelocity(0);
			player.setVelocity(0, 0);
			if (Gdx.input.isKeyJustPressed(Keys.R))
				respawn();
		}

		world.step(Math.min(1 / 60f, delta), 6, 2);
	}

	public Body addEntity(Entity e, float width, float height, float spawnX, float spawnY, float x, float y) {
		return e.createBody(world.createBody(e.getBodyDef(spawnX, spawnY)), width / 2f, height / 2f, x, y);
	}

	public float cameraScale(boolean width) {
		return width ? (textCamera.viewportWidth / camera.viewportWidth)
				: (textCamera.viewportHeight / camera.viewportHeight);
	}

	public void pullAmp(float delta) {
		// amp.changeCollisionFilters(Units.ENTITY_FLAG, Units.ALL_FLAG);
		cable.getJoint().setMaxLength(cable.getJoint().getMaxLength() - (delta * 2f));
	}

	public void cutCable() {
		world.destroyJoint(cable.getJoint());
		cable.setState(1);
	}

	// TODO: Delete
	public void respawn() {
		player.setHP(4);
		player.getBody().setTransform(spawn, 0);
		player.setVelocity(0, 0);
		input.addProcessor(pIn);
		input.addProcessor(info.getStage());
		info.setStatus("");
		flicker();
	}

	public void dispose() {
		players.forEach(Player::dispose);
		info.dispose();
		world.dispose();
		player.dispose();
		map.dispose();
		debugRenderer.dispose();
		playerAtlas.dispose();
		pause.dispose();
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

	public class CameraHelper {

		float[] samples;
		float internalTimer = 0;
		float shakeDuration = 0;
		private float intensity;

		int duration = 5; // In seconds, make longer if you want more variation
		int frequency = 35; // hertz
		float amplitude = 2; // how much you want to shake
		boolean falloff = true; // if the shake should decay as it expires

		int sampleCount;

		public CameraHelper() {
			sampleCount = duration * frequency;
			samples = new float[sampleCount];
			for (int i = 0; i < sampleCount; i++) {
				samples[i] = MathUtils.random() * 2f - 1f;
			}
			intensity = (2f);
		}

		/**
		 * Called every frame will shake the camera if it has a shake duration
		 *
		 * @param camera your camera
		 * @param delta     Gdx.graphics.getDeltaTime() or your dt in seconds
		 */
		public void update(OrthographicCamera camera, float x, float y, float delta) {
			internalTimer += delta;
			if (internalTimer > duration)
				internalTimer -= duration;
			if (isShaking()) {
				shakeDuration -= delta;
				float shakeTime = (internalTimer * frequency);
				int first = (int) shakeTime;
				int second = (first + 1) % sampleCount;
				int third = (first + 2) % sampleCount;
				float deltaT = shakeTime - (int) shakeTime;
				float deltaX = samples[first] * deltaT + samples[second] * (1f - deltaT);
				float deltaY = samples[second] * deltaT + samples[third] * (1f - deltaT);

				camera.position.x = x + deltaX * amplitude * (falloff ? Math.min(shakeDuration, 1f) : 1f);
				camera.position.y = y + deltaY * amplitude * (falloff ? Math.min(shakeDuration, 1f) : 1f);
				camera.update();
			}
		}

		/**
		 * Will make the camera shake for the duration passed in in seconds
		 *
		 * @param d duration of the shake in seconds
		 * @param i the intensity of the shake
		 */
		public void shake(float d, float i) {
			shakeDuration = d;
			intensity = i;
		}

		public boolean isShaking() {
			return shakeDuration > 0;
		}
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
		hitboxDef.filter.maskBits = Units.ENTITY_FLAG; //Units.ENTITY_FLAG;
		hitboxDef.isSensor = true;

		//hitDef.type = BodyDef.BodyType.KinematicBody;
		hitDef.type = BodyDef.BodyType.DynamicBody;
		hitDef.position.set(0, 0);
		hitDef.fixedRotation = true;
		hitDef.gravityScale = 0;

		hitSensor = world.createBody(hitDef);
		hitSensor.createFixture(hitboxDef).setUserData("hit"); // "hit"

		hitboxShape.dispose();
	}
}
