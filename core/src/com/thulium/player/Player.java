package com.thulium.player;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Elastic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.thulium.entity.Entity;
import com.thulium.util.SpriteAccessor;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isPaused;
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
	private boolean jumped;
	private boolean noJump = true;
	private float chargeTime;
	private int HP;

	public Player(TextureAtlas atlas) {
		super(atlas, 3, 3);
		setOriginCenter();

		HP = 4;
		animate("idle", 1, true);
	}

	public void render(Batch batch) {
		super.render(batch);
	}

	public void update(float delta) {
		super.update(delta);
		updateAnimation();

		if (getAnimationName().equals("rare"))
			chargeTime += delta;
		if (isPositionLocked()) {
			setPosition((getBody().getPosition().x) - (getWidth() / 2f),
					(getBody().getPosition().y) - (getHeight() / 2f));
		}
		if (isPullingAmp) {
			getBody().setTransform(getLockedPosition(), 0);
		}
	}
	
	public void attack(boolean attack) {
		chargeTime = 0;
		if (attack) {
			animate("attack", .125f, false);
		} else {
			animate("rare", .5f, true);
		}
		setPositionLocked(!attack);
	}

	@Override
	public void animate(String animationName, float speed, boolean looping) {
		super.animate(animationName, speed, looping);
	}

	// @Override
	public void updateAnimation() {
		if (overrideAnimation() && getStateTime() < 1)
			return;

			// Process jump animations
//		if (body.getLinearVelocity().y > .1f)
//			animate("jump_up", 1, true);
//		else if (body.getLinearVelocity().y < - 1f)
//			animate("jump_down", 1, true);
		if (getAnimationName().equals("jump_down") || getAnimationName().equals("jump_up")) {
			animate("idle", .15f, true);
		}

		// Process run/stop animations
		if (getBody().getLinearVelocity().y == 0 && Math.abs(getBody().getLinearVelocity().x) > .01f)
			animate("run", .1f, true);
		else if (inMargin(getBody().getLinearVelocity().x) && getAnimationName().equals("run"))
			animate("idle", .15f, true);

		//super.updateAnimation();
		if (getBody().getLinearVelocity().y > .1f)
			animate("jump_up", 1, true);
		else if (getBody().getLinearVelocity().y < - 1f)
			animate("jump_down", 1, true);

		if (getStateTime() > 1 && !Units.isLooping(getAnimationName())) {
			animate("idle", .15f, true);
		}
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
	}

	public void jump() {
		if (!isOnGround && noJump) {
			System.out.println("cant jump because");
			System.out.println("is on ground: " + isOnGround + " and has jump? " + !noJump);
			System.out.println();
			return;
		}

		float modifier = 1;
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);
		// pullAmp(false);
		jumped = !isOnGround;
	}

	public void push() {

	}

	public void powerslide() {
		Vector2 force = getBody().getLinearVelocity().scl(1.5f, 1);//new Vector2(5 / getBody().getLinearVelocity().x, 0);
		setXVelocity(0);

		getBody().applyLinearImpulse(force, getBody().getWorldCenter(), true);
		applyOpposingForce();
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
//
//		if (isOnGround && overrideAnimation())
//			setPositionLocked(true);
	}

	public boolean canPullAmp() {
		return isOnGround;
	}

	public void pullAmp(boolean isPullingAmp) {
		// setMass(isPullingAmp && isOnGround ? 100 : 1);
		setPositionLocked(isPullingAmp);
		setFriction(isPullingAmp && isOnGround ? Float.MAX_VALUE : 1);
		this.isPullingAmp = (isPullingAmp && isOnGround);
	}

	public boolean isPullingAmp() {
		return isPullingAmp;
	}

	public float getChargeTime() {
		return chargeTime;
	}

	public void setFriction(float friction) {
		getBody().getFixtureList().forEach(f -> {
			f.setFriction(friction);
		});
//		MassData massData = getBody().getMassData();
//		massData.mass = getOriginalMass() * massMul;
//		getBody().setMassData(massData);
	}

	public void setOriginalMass(float mass) {
		MassData massData = getBody().getMassData();
		massData.mass = getOriginalMass() * mass;
		getBody().setMassData(massData);
		super.setOriginalMass(getOriginalMass() * mass);
	}

	public void damage(int damage) {
		HP = MathUtils.clamp(HP - damage, 0, 4);
	}

	public int getHP() {
		return HP;
	}

	public void setHP(int HP) {
		this.HP = HP;
	}
	
	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging() {
		isDebugging = !isDebugging;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused() {
		isPaused = !isPaused;
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFlipState(boolean isFlipped) {
		super.setFlipState(isFlipped);
	}
}
