package com.thulium.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thulium.entity.Amp;
import com.thulium.entity.Cable;
import com.thulium.entity.Entity;
import com.thulium.main.MainGame;
import com.thulium.player.Player;
import com.thulium.player.PlayerControllerInput;
import com.thulium.player.PlayerInfo;
import com.thulium.player.PlayerInput;
import com.thulium.util.Jukebox;
import com.thulium.util.MyContactListener;
import com.thulium.util.Units;

import java.util.Arrays;

public class GameWorld {
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
	
	// TODO: Delete
	private PlayerInfo info;
	private ShapeRenderer shapes;
	
	private TextureAtlas playerAtlas;

	private Jukebox jukebox;
	private PauseMenu pause;

	public GameWorld(MainGame game) {
		Box2D.init();

		jukebox = new Jukebox();
		jukebox.playMusic(1);

		pause = new PauseMenu(game.getSkin());

		viewport = new StretchViewport(Units.WIDTH, Units.HEIGHT);
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

		// TODO: Delete
		addEntity(player, .3f, .2f, 0, -.4f);
		player.setOriginalMass(5);

		// Testing 1/4 cable 
		amp = new Amp(playerAtlas.findRegion("amp"));
		amp.createBody(world.createBody(amp.getBodyDef(spawn.x + 2, spawn.y)), "amp", .4f, .4f, true);
		amp.setOriginalMass(1.1f);

		cable = new Cable();
		cable.setJoint(world.createJoint(cable.getBodyDef(amp.getBody(), player.getBody())));
		
		// Test rendering game info
		info = new PlayerInfo(player, game.getSkin());
	
		shapes = new ShapeRenderer();

		groundBox.dispose();

		PlayerInput pIn = new PlayerInput(player);
		pIn.setAmp(amp);
		pIn.setCable(cable);

		// TODO: Delete
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

		fixtureDef.shape = cs; //box;
		fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
		fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.ALL_FLAG;

		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		world.createBody(bodyDef).createFixture(fixtureDef);
		// TODO: End delete

		debugRenderer = new Box2DDebugRenderer();
		Gdx.input.setInputProcessor(new InputMultiplexer(pIn, info.getStage()));
		Controllers.addListener(new PlayerControllerInput(pIn));
	}

	public void render(Batch batch, float delta) {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		map.renderBG(batch, camera);
		batch.end();

		map.render(camera, 0, 1, 2, 3);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		amp.render(batch);
		player.render(batch);

		map.renderFG(batch, camera);
		batch.end();

		shapes.setProjectionMatrix(camera.combined);
		shapes.setColor(Color.BLACK);
		shapes.begin(ShapeType.Filled);
		Array<Joint> joints = new Array<>();
		world.getJoints(joints);
		joints.forEach(j -> {
			shapes.rectLine(j.getAnchorA(), j.getAnchorB(), .025f);
		});
		// Draw player's charge under player
		if (player.getChargeTime() > 0) {
			shapes.setColor(Color.RED);
			shapes.rect(player.getX(), player.getY() - .25F, player.getWidth(), .1f);
			shapes.setColor(Color.GREEN);
			shapes.rect(player.getX(), player.getY() - .25F, Math.min(player.getChargeTime() / Units.MAX_CHARGE, 1), .1f);
		}
		shapes.end();

		map.render(camera, 4);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		map.renderFG(batch, camera);
		batch.end();

		info.render(delta);
		
		if (player.isDebugging())
			debugRenderer.render(world, camera.combined);
	}

	public void update(float delta) {
		/// TODO: Consider moving this to a player state
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			pause.show();
		if (pause.isShowing())
			return;

		camera.position.set(MathUtils.clamp(player.getBody().getPosition().x, camera.viewportWidth / 2f,
				map.getProperty("width", Integer.class)/2f - camera.viewportWidth/2f),
				MathUtils.clamp(player.getBody().getPosition().y, camera.viewportHeight / 2f,
						map.getProperty("height", Integer.class)), 0);
		// camera.position.set(player.getBody().getPosition().x, player.getBody().getPosition().y, 0);
		textCamera.position.set(camera.position.x * (cameraScale(true)), camera.position.y * (cameraScale(false)), 0);

		player.setOnGround(cl.isOnGround());
		player.update(Gdx.graphics.getDeltaTime());
		textCamera.update();
		camera.update();

		jukebox.setVolume(player.isPaused() ? 0 : 1);

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

		// TODO: Change collisioin filters for all entities in loop
		if (player.getBody().getLinearVelocity().y >= .001f) {
			player.changeCollisionFilters(Units.ENTITY_FLAG, (short)0);
			// player.changeCollisionGroup((short) 2);
		} else {
			player.changeCollisionFilters(Units.ENTITY_FLAG, Units.GROUND_FLAG);
			// player.changeCollisionGroup((short) 1);
		}

		if (!amp.isStateLocked()) {
			if (amp.getBody().getLinearVelocity().y >= .001f) {
				amp.changeCollisionFilters(Units.ENTITY_FLAG, (short)0);
				// player.changeCollisionGroup((short) 2);
			} else {
				amp.changeCollisionFilters(Units.ENTITY_FLAG, Units.GROUND_FLAG);
				// player.changeCollisionGroup((short) 1);
			}
		}

		player.changeCollisionGroup(player.getBody().getLinearVelocity().y >= .001f
				|| player.getBody().getPosition().y < amp.getBody().getPosition().y + .6f
				? (short) 2 : (short) 1);

		world.step(/**1 / 60f**/Math.min(1 / 60f, delta), 6, 2);
	}

	public void addEntity(Entity e, float width, float height) {
		e.createBody(world.createBody(e.getBodyDef(spawn.x, spawn.y)), width / 2f, height / 2f);
	}

	public void addEntity(Entity e, float width, float height, float x, float y) {
		e.createBody(world.createBody(e.getBodyDef(spawn.x, spawn.y)), width / 2f, height / 2f, x, y);
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

	public void dispose() {
		info.dispose();
		world.dispose();
		player.dispose();
		map.dispose();
		debugRenderer.dispose();
		playerAtlas.dispose();
		pause.dispose();
	}
}
