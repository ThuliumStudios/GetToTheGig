package com.thulium.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thulium.main.MainGame;
import com.thulium.util.Units;

public class GameMap {
	private OrthogonalTiledMapRenderer mapRenderer;
	private TiledMap map;
	private TmxMapLoader loader;
	
	private ParallaxBackground bg;

	public GameMap(MainGame game, Batch batch) {
		loader = new TmxMapLoader();
		map = loader.load("maps/map.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1/64f, batch);
		
		bg = new ParallaxBackground(game.getAsset("maps/snowymountains.png", Texture.class), 
				game.getAsset("maps/whiteclouds.png", Texture.class));
	}
	
	public void render(OrthographicCamera camera) {
		camera.position.set(camera.position.x, camera.position.y, 0);
		mapRenderer.setView(camera);
		mapRenderer.render();
	}

	public void render(OrthographicCamera camera, int... layers) {
		camera.position.set(camera.position.x, camera.position.y, 0);
		mapRenderer.setView(camera);
		mapRenderer.render(layers);
	}
	
	public void renderBG(Batch batch, OrthographicCamera camera) {
		bg.render(batch, camera);
	}

	public void createBox2dObjects(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("collision");
		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				Cell cell = layer.getCell(x, y);

				if (cell == null || cell.getTile() == null)
					continue;
				bodyDef.type = BodyType.StaticBody;
				bodyDef.position.set((x + .5f), (y + .5f));

				ChainShape cs = new ChainShape();
				Vector2[] v = new Vector2[5];
				v[0] = new Vector2((-1 / 2f), (-1 / 2f));
				v[1] = new Vector2((-1 / 2f), (1 / 2f));
				v[2] = new Vector2((1 / 2f), (1 / 2f));
				v[3] = new Vector2((1 / 2f), (-1 / 2f));
				v[4] = new Vector2(v[0]);
				
				cs.createChain(v);
				fixtureDef.friction = .25f;
				fixtureDef.shape = cs;
				fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
				fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.ALL_FLAG;
				fixtureDef.isSensor = false;
				world.createBody(bodyDef).createFixture(fixtureDef);
				cs.dispose();
			}
		}
	}

	public <T> T getProperty(String property, Class<T> classType) {
		System.out.println("Property " + property + " is " + map.getProperties().get(""));
		return map.getProperties().get("", classType);
	}

	public void dispose() {
		map.dispose();
		mapRenderer.dispose();
	}
	
	/*
	 * Parrax background inner class
	 */
	private class ParallaxBackground {
		private OrthographicCamera bgCamera1, bgCamera2;
		private Sprite bg1, bg2, accent1, accent2;

		public ParallaxBackground(Texture bg1Texture, Texture bg2Texture) {
			bgCamera1 = new OrthographicCamera();
			bgCamera2 = new OrthographicCamera();
			Viewport viewport = new StretchViewport(Units.WIDTH, Units.HEIGHT);
			Viewport viewport2 = new StretchViewport(Units.WIDTH, Units.HEIGHT);
			viewport.setCamera(bgCamera1);
			viewport.apply();
			viewport2.setCamera(bgCamera2);
			viewport2.apply();
			bgCamera1.position.set(-Units.WIDTH * 2, Units.HEIGHT / 2 - 1, 0);
			bgCamera2.position.set(-Units.WIDTH / 2 - Units.WIDTH * 2, Units.HEIGHT / 2 - 1, 0);
			bgCamera1.update();
			bgCamera2.update();

			bg1 = new Sprite(bg1Texture);
			bg2 = new Sprite(bg1Texture);
			accent1 = new Sprite(bg2Texture);
			accent2 = new Sprite(bg2Texture);

			bg1.setBounds(-viewport.getWorldWidth(), -1, Units.WIDTH * 1.5f, Units.WIDTH / 3f);
			bg2.setBounds(bg1.getX() + bg1.getWidth(), -1, Units.WIDTH * 1.5f, Units.WIDTH / 3f);
			accent1.setBounds(-viewport.getWorldWidth(), -1, Units.WIDTH * 1.5f, Units.WIDTH / 3f);
			accent2.setBounds(bg1.getX() + bg1.getWidth(), -1, Units.WIDTH * 1.5f, Units.WIDTH / 3f);
		}

		public void render(Batch batch, OrthographicCamera camera) {
			bgCamera1.position.set(camera.position.x / 8, (camera.position.y / 8) + (camera.viewportHeight / 3f), 0);
			bgCamera2.position.set(camera.position.x / 4 - Units.WIDTH * 2,
					(camera.position.y / 4) + (camera.viewportHeight / 3f), 0);

			batch.setProjectionMatrix(bgCamera1.combined);
			bg1.draw(batch);
			bg2.draw(batch);
			batch.setProjectionMatrix(bgCamera2.combined);
			accent1.draw(batch);
			accent2.draw(batch);
			bgCamera1.update();
			bgCamera2.update();

			checkBackground();
			
			accent1.translateX(-Gdx.graphics.getDeltaTime() / 3);
			accent2.translateX(-Gdx.graphics.getDeltaTime() / 3);
		}

		public void checkBackground() {
			if (bg1.getX() < (bgCamera1.position.x - bgCamera1.viewportWidth / 2f) - bg1.getWidth())
				bg1.setX(bg2.getX() + bg1.getWidth());
			else if (bg2.getX() < (bgCamera1.position.x - bgCamera1.viewportWidth / 2f) - bg2.getWidth())
				bg2.setX(bg1.getX() + bg2.getWidth());

			if (accent1.getX() < (bgCamera2.position.x - bgCamera2.viewportWidth / 2f) - accent1.getWidth())
				accent1.setPosition(accent2.getX() + accent1.getWidth(), MathUtils.random(4));
			else if (accent2.getX() < (bgCamera2.position.x - bgCamera2.viewportWidth / 2f) - accent2.getWidth())
				accent2.setPosition(accent1.getX() + accent2.getWidth(), MathUtils.random(4));
		}
	}
}
