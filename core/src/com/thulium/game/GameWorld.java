package com.thulium.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.thulium.util.MyContactListener;
import com.thulium.util.Units;

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

	public GameWorld(MainGame game) {
		Box2D.init();

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

		spawn = new Vector2(2.5f, 12.5f);

		playerAtlas = new TextureAtlas(Gdx.files.internal("img/player.atlas"));
		player = new Player(playerAtlas);
		
		addEntity(player, .6f, .2f, 0, -.4f);
		
		// Testing 1/4 cable 
		amp = new Amp(playerAtlas.findRegion("amp"));
		amp.createBody(world.createBody(amp.getBodyDef(spawn.x + 2, spawn.y)), "amp", .4f, .4f, true);
		
		cable = new Cable();
		cable.setJoint(world.createJoint(cable.getBodyDef(amp.getBody(), player.getBody())));
		
		// Test rendering game info
		info = new PlayerInfo(player, game.getSkin());
	
		shapes = new ShapeRenderer();

		groundBox.dispose();

		PlayerInput pIn = new PlayerInput(player);
		pIn.setAmp(amp);
		pIn.setCable(cable);

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
		shapes.end();

		map.render(camera, 4);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		map.renderFG(batch, camera);
		batch.end();

		info.render(delta);

		if (player.isPullingAmp()) {
			if (cable.getJoint().getMaxLength() > 0) {
				pullAmp(delta);
			}
		}

		// TODO: Delet dis -.-
		if (Gdx.input.isKeyJustPressed(Keys.P)) {
			cutCable();
		}

		if (player.getBody().getLinearVelocity().y >= .001f) {
			player.changeCollisionFilters(Units.ENTITY_FLAG, (short)0);
			// player.changeCollisionGroup((short) 2);
		} else {
			player.changeCollisionFilters(Units.ENTITY_FLAG, Units.GROUND_FLAG);
			// player.changeCollisionGroup((short) 1);
		}

		player.changeCollisionGroup(player.getBody().getLinearVelocity().y >= .001f
				|| player.getBody().getPosition().y < amp.getBody().getPosition().y + .6f
				? (short) 2 : (short) 1);
		
		if (player.isDebugging())
			debugRenderer.render(world, camera.combined);
//		if (Gdx.input.isKeyJustPressed(Keys.K))
//			amp.kick(player.getBody().getPosition().x < amp.getBody().getPosition().x ? 1 : -1);
	}

	public void update(float delta) {
		camera.position.set(MathUtils.clamp(player.getBody().getPosition().x, camera.viewportWidth / 2f,
				map.getProperty("width", Integer.class) - camera.viewportWidth/2f),
				MathUtils.clamp(player.getBody().getPosition().y, 0,
						map.getProperty("height", Integer.class)), 0);
		textCamera.position.set(camera.position.x * (cameraScale(true)), camera.position.y * (cameraScale(false)), 0);

		player.setOnGround(cl.isOnGround());
		player.update(Gdx.graphics.getDeltaTime());
		textCamera.update();
		camera.update();

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
		cable.getJoint().setMaxLength(cable.getJoint().getMaxLength() - delta);
	}

	public void cutCable() {
		world.destroyJoint(cable.getJoint());
	}

	public float getTimestsep() {
		return 1 / 10f;
	}

	public void dispose() {
		info.dispose();
		world.dispose();
		player.dispose();
		map.dispose();
		debugRenderer.dispose();
		playerAtlas.dispose();
	}
}
