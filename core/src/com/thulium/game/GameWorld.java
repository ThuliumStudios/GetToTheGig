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

		gravity = new Vector2(0, -29.43f);
		world = new World(gravity, true);
		world.setContactListener(cl);

		BodyDef groundBodyDef = new BodyDef();
		PolygonShape groundBox = new PolygonShape();
		FixtureDef groundDef = new FixtureDef();

		map = new GameMap(game, game.getBatch());
		map.createBox2dObjects(world, groundBodyDef, groundDef);

		spawn = new Vector2(16.5f, 6.5f);

		playerAtlas = new TextureAtlas(Gdx.files.internal("img/player.atlas"));
		player = new Player(playerAtlas);
		
		addEntity(player, .6f, .9f);
		
		// Testing 1/4 cable 
		amp = new Amp(playerAtlas.findRegion("amp"));
		amp.createBody(world.createBody(amp.getBodyDef(spawn.x + 2, spawn.y - 3)), "amp", .4f, .4f, true);
		
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
		
		map.render(camera, 0, 1, 2);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		amp.render(batch);
		player.render(batch);
		batch.end();
		
		shapes.setProjectionMatrix(camera.combined);
		shapes.setColor(Color.BLACK);
		shapes.begin(ShapeType.Filled);
		shapes.rectLine(player.getBody().getPosition().x, player.getBody().getPosition().y, amp.getBody().getPosition().x, amp.getBody().getPosition().y, .025f);
		shapes.end();

		map.render(camera, 3);
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

		// Attempt to adjust amp collision filter based on player's Y velocity
		if (player.getBody().getLinearVelocity().y < -.05f) {
			changeAmpState(Units.ALL_FLAG, Units.NONE_FLAG);
		} else if (player.getBody().getLinearVelocity().y > .05f) {
			System.out.println("Should not be colliding");
			changeAmpState(Units.ENTITY_FLAG, Units.GROUND_FLAG);
		}

		// TODO: End delete block

//		Fixture f = amp.getBody().getFixtureList().first
//		();
//		Filter fd = f.getFilterData();
//		if (player.getBody().getLinearVelocity().y <= 0) {
//			fd.categoryBits = Units.GROUND_FLAG | Units.ENTITY_FLAG;
//			fd.maskBits = Units.GROUND_FLAG;
//		} else {
//			fd.groupIndex = 1;
//		}
		
		if (player.isDebugging())
			debugRenderer.render(world, camera.combined);
//		if (Gdx.input.isKeyJustPressed(Keys.K))
//			amp.kick(player.getBody().getPosition().x < amp.getBody().getPosition().x ? 1 : -1);
	}

	public void update() {
		camera.position.set(player.getBody().getPosition(), 0);
		textCamera.position.set(camera.position.x * (cameraScale(true)), camera.position.y * (cameraScale(false)), 0);

		player.setOnGround(cl.isOnGround());
		player.update(Gdx.graphics.getDeltaTime());
		textCamera.update();
		camera.update();

		world.step(1 / 60f, 6, 2);
	}

	public void addEntity(Entity e, float width, float height) {
		e.createBody(world.createBody(e.getBodyDef(spawn.x, spawn.y)), width / 2f, height / 2f);
	}

	public float cameraScale(boolean width) {
		return width ? (textCamera.viewportWidth / camera.viewportWidth)
				: (textCamera.viewportHeight / camera.viewportHeight);
	}

	// Used to change collision properties of the amp depending on player's movement
	public void changeAmpState(short categoryBits, short maskBits) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				amp.getBody().getFixtureList().forEach(f -> {
					Filter filter = f.getFilterData();
					filter.categoryBits = categoryBits;
					filter.maskBits = maskBits;
					amp.getBody().getFixtureList().first().setFilterData(filter);
				});
			}
		});
//		amp.getBody().getFixtureList().forEach(f -> {
//			Filter filter = f.getFilterData();
//			System.out.println("Category: " + categoryBits);
//			filter.categoryBits = categoryBits;
//			filter.maskBits = maskBits;
//			amp.getBody().getFixtureList().first().setFilterData(filter);
//		});
	}

	public void pullAmp(float delta) {
		changeAmpState(Units.ENTITY_FLAG, Units.ALL_FLAG);
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
