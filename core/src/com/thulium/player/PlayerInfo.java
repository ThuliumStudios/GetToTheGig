package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.stream.IntStream;

public class PlayerInfo {
	private Label infoLabel;
	private Label status;

	// HUD objects
	private Table hudTable;
	private Stage stage;
	private Image face;
	private Image plectrum;

	private TextureAtlas hudAtlas;

	private boolean debug;

	private int hearts;
	private Player player;

	public PlayerInfo(Player player, TextureAtlas hudAtlas, Skin skin) {
		this.player = player;
		this.hudAtlas = hudAtlas;
		stage = new Stage();

		infoLabel = new Label("" + Gdx.graphics.getFramesPerSecond(), skin);

		status = new Label("", skin);
		status.setFontScale(5);
		status.setPosition(stage.getWidth() / 4f, stage.getHeight() / 2f);

		hearts = player.getHP();
		face = new Image(hudAtlas.findRegion("hudface", player.getHP()));
		plectrum = new Image(hudAtlas.findRegion("hp"));

		hudTable = new Table(skin);
		hudTable.defaults().size(stage.getWidth() / 16f, stage.getWidth() / 16f).grow();
		hudTable.add(face).pad(stage.getHeight() / 32f);
		for (int i = 0; i < player.getHP(); i++)
			hudTable.add(new Image(hudAtlas.findRegion("hp"))).size(Value.percentWidth(3));

		Table table = new Table(skin);
		table.setFillParent(true);
		table.defaults().expandX().top().left();

		table.add(hudTable).row();
		table.add(infoLabel).expand().row();

		stage.addActor(table);
		stage.addActor(status);
	}
	
	public void render(float delta) {
		infoLabel.setText(debug ? getDebugInfo() : "");
		stage.draw();
	}

	public void update(float delta) {
		stage.act(delta);
		setHearts();
	}

	public void setHearts() {
		if (hearts == player.getHP())
			return;

		face.setDrawable(new TextureRegionDrawable(hudAtlas.findRegion("hudface", player.getHP())));
		IntStream.range(1, 5)
				.forEach(i -> hudTable.getCells().get(i).setActor(new Image(hudAtlas.findRegion("hp_empty"))));
		IntStream.range(1, player.getHP() + 1)
				.forEach(i -> hudTable.getCells().get(i).setActor(new Image(hudAtlas.findRegion("hp"))));
		hearts = player.getHP();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getDebugInfo() {
		return "FPS: " + Gdx.graphics.getFramesPerSecond() +
				"\nIs position locked? " + player.isPositionLocked() +
				"\nVelocity: " + roundedVector(player.getBody().getLinearVelocity()) +
				"\nPosition: " + roundedVector(player.getBody().getPosition()) +
				"\nAnimation: " + player.getAnimationName() +
				"\nAnimation Frame: " + player.getCurrentAnimationFrame();
	}

	public void setStatus(String status) {
		this.status.setText(status);
	}
	
	public String roundedVector(Vector2 v) {
		return String.format("%.2f", v.x) + ", " + String.format("%.2f", v.y);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void dispose() {
		stage.dispose();
	}
}
