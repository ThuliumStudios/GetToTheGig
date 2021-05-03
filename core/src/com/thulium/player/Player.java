package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Body;
import com.thulium.entity.Entity;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean jumped;
	private boolean noJump = true;

	public Player(TextureAtlas atlas) {
		super(atlas);

		animate("idle", 1, true);
	}

	public void render(Batch batch) {
		super.render(batch);
		update(Gdx.graphics.getDeltaTime());
		updateAnimation();
	}

	public void update(float delta) {
		super.update(delta);
	}
	
	public void attack() {
		animate("kick", .25f, false);
	}

	public void jump() {
		if (!isOnGround && noJump)
			return;

		float modifier = 1;
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);
		jumped = !isOnGround;
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
	}

	public void createBody(Body body) {
		super.createBody(body, "player", 16, 28, true);
	}
	
	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging() {
		isDebugging = !isDebugging;
	}

	public void dispose() {
		super.dispose();
	}
}
