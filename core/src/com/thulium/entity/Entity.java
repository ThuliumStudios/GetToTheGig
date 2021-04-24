package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.thulium.util.Units;

public class Entity extends BaseEntity {
	private Body body;
	private Vector2 velocity;

	public Entity(TextureAtlas atlas) {
		super(atlas);
		velocity = new Vector2();
	}

	public void render(Batch batch) {
		super.render(batch);
		setPosition((body.getPosition().x) - (getWidth() / 2f),
				(body.getPosition().y) - (getHeight() / 2f));
	}

	public void update(float delta) {
		super.update(delta);
//		if (Math.abs(body.getLinearVelocity().x) <= Units.MAX_VELOCITY) {
//			body.setLinearVelocity(velocity.x, body.getLinearVelocity().y);
//		}
		if (Math.abs(body.getLinearVelocity().x) <= Units.MAX_VELOCITY) 
		body.applyForceToCenter(velocity, true);
	}
	
	public void updateAnimation() {
		if (body.getLinearVelocity().y > 0)
			animate("jump_up");
		else if (body.getLinearVelocity().y < 0)
			animate("jump_down");
		else if (getAnimationName().equals("jump_down"))
			animate("idle", .3f, true);
		
		if (body.getLinearVelocity().y == 0 && Math.abs(body.getLinearVelocity().x) > 0)
			animate("run");
		else if (body.getLinearVelocity().isZero() && getAnimationName().equals("run"))
			animate("idle", .3f, true);
	}

	public BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
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
		velocity.x = x;
		
	}
	
	public void applyOpposingForce() {
		body.applyLinearImpulse(new Vector2(-body.getLinearVelocity().x / 2f, 0), body.getWorldCenter(), true);
	}
	
	public void createBody(Body body, float width, float height) {
		createBody(body, "entity", width, height, true);
	}

	public void createBody(Body body, Object name, float width, float height, boolean hasFoot) {
		this.body = body;

		PolygonShape box = new PolygonShape();
		box.setAsBox((width), (height));
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
		fixtureDef.filter.maskBits = Units.GROUND_FLAG;
		body.createFixture(fixtureDef).setUserData(name);

		if (hasFoot) {
			box.setAsBox((width * .9f), (height * .1f), new Vector2(0, (-height)), 0);
			fixtureDef.shape = box;
			fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
			fixtureDef.filter.maskBits = Units.GROUND_FLAG;
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef).setUserData("foot");
		}

		box.dispose();
	}
	
	public void dispose() {
		super.dispose();
	}
}
