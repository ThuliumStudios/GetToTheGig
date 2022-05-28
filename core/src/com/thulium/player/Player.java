package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.thulium.entity.Entity;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
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
	
	public void attack(boolean attack) {
		if (attack) {
			animate("kick", .1f, false);
		} else {
			animate("idle", 1, false);
		}
	}

	public void jump() {
		if (!isOnGround && noJump)
			return;

		float modifier = 1;
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);
		// pullAmp(false);
		jumped = !isOnGround;
	}

	public void powerslide() {
		Vector2 force = new Vector2(5 / getBody().getLinearVelocity().x, 0);
		setXVelocity(0);

		getBody().applyLinearImpulse(force, getBody().getWorldCenter(), true);
		applyOpposingForce();
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
	}

	public void pullAmp(boolean isPullingAmp) {
		setMass(isPullingAmp && isOnGround ? 100 : 1);
		this.isPullingAmp = (isPullingAmp && isOnGround);
	}

	public boolean isPullingAmp() {
		return isPullingAmp;
	}

	public void setMass(float massMul) {
		MassData massData = getBody().getMassData();
		massData.mass = getOriginalMass() * massMul;
		getBody().setMassData(massData);
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
