package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.thulium.entity.AnimationWrapper;
import com.thulium.entity.Entity;
import com.thulium.entity.Priority;
import com.thulium.util.Units;

/*
TODO:
	BUG: Collision detection, including sensors (i.e., ledge grab), only occurs when player is not jumping.
 */
public class Player extends Entity {
	// private boolean isMoving;
	private boolean canGrabLedge;
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
	private boolean jumped;
	// private boolean noJump = true;
	private boolean isCharging;
	private float charge;
	private float xVel;
	private int HP;

    // TODO: Delete all variables below here. For testing purposes only
	private final Sprite blood;
	private final Animation<TextureRegion> bloodAnim;
	private float bloodStateTime;
	private PlayerProjectile projectile;

	// TODO: Consider removing and replacing functionality
	private Vector2 ledgePoint;

	public Player(TextureAtlas atlas) {
		super(atlas, 1, 1);
		setOriginCenter();

		HP = 4;

		System.out.println("Comparing enums");
		System.out.println(Priority.High.compareTo(Priority.Low));
		System.out.println(Priority.Low.compareTo(Priority.High));

		setSize(3, 3);
		addAnimation("idle", new AnimationWrapper("idle", 1f, atlas, Priority.Bottom));
		addAnimation("run", new AnimationWrapper("run", 1/10f, atlas, Priority.Normal));
		addAnimation("attack", new AnimationWrapper("attack0", 1/13f, atlas, Priority.Top));
		addAnimation("rare", new AnimationWrapper("rare", 1/2f, atlas, Priority.High));
		addAnimation("jump_up", new AnimationWrapper("jump_up", 1f, atlas, Priority.High));
		addAnimation("jump_down", new AnimationWrapper("jump_down", 1f, atlas, Priority.High));
		addAnimation("death", new AnimationWrapper("death", 1/8f, atlas, Priority.High));
		animate("idle");

		// TODO: Delete all variable declarations below here. For testing purposes only
		blood = new Sprite();
		blood.setBounds(-1, -1, getWidth(), getHeight());
		blood.setOriginCenter();
		bloodAnim = new Animation<>(.025f, atlas.findRegions("blood"));
		bloodStateTime = 1;
	}

	public void render(Batch batch, OrthographicCamera camera, float delta) {
		super.render(batch);

		// Render projectile if in frame
		if (projectile.isInCamera(camera))
			projectile.render(batch, delta);
		renderBlood(batch, delta);
	}

	public void update(float delta) {
		super.update(delta);
		updateAnimation();

		if (isPositionLocked()) {
			setPosition((getBody().getPosition().x) - (getWidth() / 2f),
					(getBody().getPosition().y) - (getHeight() / 2f));
		} else {
			setXVelocity(xVel);
		}
		if (isPullingAmp) {
			getBody().setTransform(getLockedPosition(), 0);
		}

		if (isCharging)
			charge = MathUtils.clamp(charge + delta, 0, 1);
	}

	public void charge() {
		isCharging = true;
		animate(isOnGround() ? "rare" : "attack"); // TODO: Account for aerial attacks
	}
	
	public void attack() {
		// Throw guitar
		if (charge == 1 || !isOnGround()) {	// TODO: Change max charge to variable (possibly in @{Units})
			System.out.println("Throwing guitar");
			projectile.throwProjectile(getBody().getPosition().x, getBody().getPosition().y, isFlipped());
		}

		animate("attack"); // TODO: Animate aerial attacks
		setVelocity(0, getVelocity().y);
		getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
		getBody().setAngularVelocity(0);

		isCharging = false;
		charge = 0;
	}

	// TODO: Delete this whole method
	private void renderBlood(Batch batch, float delta) {
//		blood.setRegion(bloodAnim.getKeyFrame(bloodStateTime));
//		blood.draw(batch);
//		bloodStateTime += delta;
	}

	// @Override
	public void updateAnimation() {
		// Process run/stop animations
		if (getHP() < 1) {
			return;
		}
		if (isOnGround() && Math.abs(getBody().getLinearVelocity().x) > .01f)
			animate("run");
		else if (inMargin(getBody().getLinearVelocity().x) && getAnimationName().equals("run"))
			animate(isCharging ? "rare" : "idle");

		if (!isOnGround()) {
			if (getBody().getLinearVelocity().y > .1f)
				animate("jump_up");
			else if (getBody().getLinearVelocity().y < -1f)
				animate("jump_down");
		}

		// Check if player should be idle
		if (isOnGround()) {
			if ((isAnimationFinished() && !Units.isLooping(getAnimationName())) ||
					(isAnimation("jump_down") || isAnimation("jump_up"))) {
				animate("idle");
			}
		}

		setPositionLocked(isAnimation("attack") && !isAnimationFinished());
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
	}

	public void jump() {
		if (!isOnGround || canDoubleJump())
			return;


		float modifier = 1;	// TODO: Replace with global variable/const if needed

		// Set jump velocity regardless of whether the player is falling
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);

		// Indicate that the player has air-jumped (double-jumped)
		jumped = !isOnGround;
	}

	public void ledgeGrab() {

	}

	public void push() {

	}

	public void powerslide() {
		Vector2 force = getBody().getLinearVelocity().scl(5f, 1);//new Vector2(5 / getBody().getLinearVelocity().x, 0);
		setXVelocity(0);

		getBody().applyLinearImpulse(force, getBody().getWorldCenter(), true);
		applyOpposingForce();
	}

	public boolean isOnGround() {
		return isOnGround && Math.abs(getBody().getLinearVelocity().y) < .001f;
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
//
//		if (isOnGround && overrideAnimation())
//			setPositionLocked(true);
	}

	public boolean canDoubleJump() {
		return false;
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

		if (damage > 0 && getHP() > 0) {
			bloodStateTime = 0;
			blood.setRotation(MathUtils.random(360));
			blood.setPosition(getX(), getY());

			float rx = -.5f + MathUtils.random(), ry = MathUtils.random(-1f, .5f); // TODO: Make variable
			blood.translate(rx, ry);
			// System.out.println("Translation: " + rx + ", " + ry);
		}
	}

	// TODO: Cleanup after testing
	public void grabLedge() {
		getBody().setTransform(ledgePoint.add(0, getHeight() / 2), 0);
		System.out.println("GRABBING LEDGE");
	}

	public void setCanGrabLedge(boolean canGrabLedge) {
		this.canGrabLedge = canGrabLedge;
	}

	public boolean canGrabLedge() {
		return canGrabLedge;
	}

	public void setLedgePoint(Vector2 ledgePoint) {
		this.ledgePoint = ledgePoint;
		System.out.println("Set ledge point to " + ledgePoint);
	}

	public void switchStates(TextureAtlas atlas) {
		setAtlas(atlas);
	}

	public boolean collidesWith(Sprite sprite) {
		return getBoundingRectangle().overlaps(sprite.getBoundingRectangle());
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

	public void toggleDebug() {
		isDebugging = !isDebugging;
		System.out.println("Debugging? " + isDebugging);

	}

	public boolean isCharging() {
		return isCharging;
	}

	public float getCharge() {
		return charge;
	}

	public void dispose() {
		super.dispose();
	}

	public void setProjectile(PlayerProjectile projectile) {
		this.projectile = projectile;
	}

	@Override
	public void setFlipState(boolean isFlipped) {
		super.setFlipState(isFlipped);
	}

	@Override
	public void setXVelocity(float x) {
		super.setXVelocity(x);
		xVel = x;
	}

	public Body createBody(Body body, Object name, float width, float height, float x, float y, boolean hasFoot) {
		super.createBody(body, name, width, height, x, y, hasFoot);

		float w = .3f;
		float h = .5f;

		// Create 'left hand' sensor
		PolygonShape box = new PolygonShape();
		box.setAsBox(w, h, new Vector2(x - .4f, y + 1), 0);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
		fixtureDef.filter.maskBits = Units.GROUND_FLAG | Units.PLAYER_FLAG;
		fixtureDef.filter.groupIndex = 2;
		fixtureDef.isSensor = true;
		body.createFixture(fixtureDef).setUserData("hand");

		// Create 'right hand' sensor
		box.setAsBox(w, h, new Vector2(x + .4f, y + 1), 0);
		body.createFixture(fixtureDef).setUserData("hand");

		box.dispose();
		return body;
	}
}
