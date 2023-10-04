package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.thulium.util.Units;

public class Entity extends BaseEntity {
	private Body body;
	private Vector2 velocity;
	private Vector2 lockedPosition;
	private boolean isPositionLocked;
	private float originalMass;

	// TODO: Delete
	private final String[] priorityAnimations = {"rare", "attack"};

	public Entity(TextureAtlas atlas) {
		super(atlas);
		velocity = new Vector2();
		lockedPosition = new Vector2();
	}

	public void render(Batch batch) {
		super.render(batch);
		setPosition((body.getPosition().x) - (getWidth() / 2f),
				(body.getPosition().y) - (getHeight() / 2f));
	}

	public void update(float delta) {
		super.update(delta);
		updateAnimation();
		if (Math.abs(body.getLinearVelocity().x) <= Units.MAX_VELOCITY && !isPositionLocked)
			body.applyForceToCenter(velocity, true);
	}
	
	public void updateAnimation() {

		if (overrideAnimation())
			return;

		// Process jump animations
//		if (body.getLinearVelocity().y > .1f)
//			animate("jump_up", 1, true);
//		else if (body.getLinearVelocity().y < - 1f)
//			animate("jump_down", 1, true);
		else if (getAnimationName().equals("jump_down") || getAnimationName().equals("jump_up")) {
			animate("idle", .15f, true);
		}
		
		// Process run/stop animations
		if (body.getLinearVelocity().y == 0 && Math.abs(body.getLinearVelocity().x) > .01f)
			animate("run", .1f, true);
		else if (inMargin(body.getLinearVelocity().x) && getAnimationName().equals("run"))
			animate("idle", .15f, true);
	}

	public boolean overrideAnimation() {
		for (String s : priorityAnimations) {
			if (getAnimationName().equals(s))
				return true;
		}
		return false;
	}

	public void setPositionLocked(boolean isPositionLocked) {
		this.isPositionLocked = isPositionLocked;
		lockedPosition.set(body.getPosition());
	}

	public boolean isPositionLocked() {
		return isPositionLocked;
	}

	public Vector2 getLockedPosition() {
		return lockedPosition;
	}

	public BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.fixedRotation = true;
		return bodyDef;
	}

	public Body getBody() {
		return body;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(float x, int y) {
		velocity.set(x, y);
	}
	
	public void setXVelocity(float x) {
		velocity.x = x * 3f;
	}

	public void applyOpposingForce() {
		body.applyLinearImpulse(new Vector2(-body.getLinearVelocity().x / 2f * body.getMass(), 0),
				body.getWorldCenter(), true);
	}

	public boolean inMargin(float value) {
		return value < .01f && value > -.01f;
	}

	public float getOriginalMass() { return originalMass; }

	public void setOriginalMass(float originalMass) {
		this.originalMass = originalMass;
	}

	public void createBody(Body body, float width, float height) {
		createBody(body, "entity", width, height, width, height, true);
	}

	public Body createBody(Body body, float width, float height, float x, float y) {
		return createBody(body, "entity", width, height, x, y, true);
	}

	public void createBody(Body body, Object name, float width, float height, boolean hasFoot) {
		createBody(body, "entity", width, height, width, height, hasFoot);
	}

	public Body createBody(Body body, Object name, float width, float height, float x, float y, boolean hasFoot) {
		this.body = body;

		PolygonShape box = new PolygonShape();
		box.setAsBox((width), (height), new Vector2(x, y), 0);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
		fixtureDef.filter.maskBits = Units.GROUND_FLAG | Units.ALL_FLAG;
		fixtureDef.filter.groupIndex = 2;
		fixtureDef.friction = 1;
		fixtureDef.density = 1.5f;
		body.createFixture(fixtureDef).setUserData(name);
		originalMass = body.getMass();

		System.out.println(originalMass);

		if (hasFoot) {
			box.setAsBox((width * .9f),.05f, new Vector2(x, y - height), 0);
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef).setUserData("foot");
		}

		box.dispose();
		return body;
	}

	// TODO: Possibly delete and replace
	public void changeMass(float density) {
		body.getFixtureList().forEach(f -> {
			body.getFixtureList().first().setDensity(density);
			body.resetMassData();
		});
	}

	public void changeCollisionFilters(short categoryBits, short maskBits) {
		body.getFixtureList().forEach(f -> {
			Filter filter = f.getFilterData();
			filter.categoryBits = categoryBits;
			filter.maskBits = maskBits;
			body.getFixtureList().first().setFilterData(filter);
		});
	}

	public void changeCollisionGroup(short group) {
		body.getFixtureList().forEach(f -> {
			Filter filter = f.getFilterData();
			filter.groupIndex = group;
			body.getFixtureList().first().setFilterData(filter);
		});
	}
	
	public void dispose() {
		super.dispose();
	}
}
