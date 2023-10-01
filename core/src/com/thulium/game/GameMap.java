package com.thulium.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
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
import com.badlogic.gdx.utils.Array;
import com.thulium.main.MainGame;
import com.thulium.util.Units;

import java.util.Arrays;

public class GameMap {
	private OrthogonalTiledMapRenderer mapRenderer;
	private TiledMap map;
	private TmxMapLoader loader;
	
	private ParallaxScene parallax;

	public GameMap(MainGame game, Batch batch) {
		loader = new TmxMapLoader();
		map = loader.load("maps/map.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1/32f / 2f, batch);

		parallax = new ParallaxScene();
		parallax.addBackgrounds(game.getAsset("maps/BG_Decor.png", Texture.class),
				game.getAsset("maps/Middle_Decor.png", Texture.class));
		// parallax.addForegrounds(game.getAsset("maps/whiteclouds.png", Texture.class));
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
		parallax.renderBG(batch, camera);
	}

	public void renderFG(Batch batch, OrthographicCamera camera) {
		parallax.renderFG(batch, camera);
	}

	public void createBox2dObjects(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("collision");
		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				Cell cell = layer.getCell(x, y);

				if (cell == null || cell.getTile() == null)
					continue;
				bodyDef.type = BodyType.StaticBody;
				// bodyDef.position.set((x + .5f), (y + .5f));

				// Only create collision objects at the top of tiles
				//bodyDef.position.set(x + .5f, y + .8f);
				bodyDef.position.set((x + .5f) / 2f, (y + .8f) / 2f);


				ChainShape cs = new ChainShape();
				Vector2[] v = new Vector2[5];
				v[0] = new Vector2((-1 / 2f), (-1 / 8f));
				v[1] = new Vector2((-1 / 2f), (1 / 8f));
				v[2] = new Vector2((1 / 2f), (1 / 8f));
				v[3] = new Vector2((1 / 2f), (-1 / 8f));
				v[4] = new Vector2(v[0]);

				Arrays.asList(v).forEach(vec -> vec.scl(.5f));
				
				cs.createChain(v);
				fixtureDef.friction = .1f;//.5f;
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
		return map.getProperties().get(property, classType);
	}

	public void dispose() {
		map.dispose();
		mapRenderer.dispose();
	}
	
	/*
	 * Parrax background inner class
	 */
	private class ParallaxScene {
		private Array<TextureRegion> fgs, bgs;
		private float mul = 512f;

		public ParallaxScene() {
			bgs = new Array<>();
			fgs = new Array<>();
		}

		public void renderBG(Batch batch, OrthographicCamera camera) {
			batch.setColor(1, 1, 1, .25f);
			bgs.forEach(bg -> {
				int i = bgs.indexOf(bg, true);
				bg.setRegionX((int) (camera.position.x * Math.sqrt(mul) * i) % bg.getTexture().getWidth());
				bg.setRegionWidth((int) (bg.getTexture().getWidth()));

				batch.draw(bg, camera.position.x - camera.viewportWidth/2f, camera.position.y - camera.viewportHeight / 2f,
						Units.WIDTH, Units.HEIGHT);
			});
			batch.setColor(Color.WHITE);
		}

		public void renderFG(Batch batch, OrthographicCamera camera) {
			fgs.forEach(fg -> {
				int i = fgs.indexOf(fg, true);
				fg.setRegionX((int) (camera.position.x * mul) % fg.getTexture().getWidth());
				fg.setRegionWidth((int) (fg.getTexture().getWidth()));

				// TODO: Delete
				fg.setRegionX((int) (camera.position.x * mul) % fg.getTexture().getWidth());
				fg.setRegionWidth((int) (fg.getTexture().getWidth()));

				batch.draw(fg, camera.position.x - camera.viewportWidth/2f, camera.position.y - camera.viewportHeight / 2f,
						Units.WIDTH, Units.HEIGHT);
			});
		}

		public void addBackgrounds(Texture... bgsToAdd) {
			Arrays.asList(bgsToAdd).forEach(bg -> {
				 bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
				 bgs.add(new TextureRegion(bg));
			});
		}

		public void addForegrounds(Texture... fgsToAdd) {
			Arrays.asList(fgsToAdd).forEach(fg -> {
				fg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
				fgs.add(new TextureRegion(fg));
			});
		}
	}
}
