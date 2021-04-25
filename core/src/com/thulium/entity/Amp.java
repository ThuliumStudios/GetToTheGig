package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.thulium.util.Units;

public class Amp {
	private Sprite sprite;
	private Body body;

	public Amp(TextureRegion region) {
		sprite = new Sprite(region);
		sprite.setSize(1, 1);
	}

	public void render(Batch batch) {
		sprite.draw(batch);
		sprite.setPosition((body.getPosition().x) - (sprite.getWidth() / 2f),
				(body.getPosition().y) - (sprite.getHeight() / 2f));
	}
	
	public void kick(float x) {
		body.applyLinearImpulse(new Vector2(10 * x, 10), body.getWorldCenter(), true);
	}
	
	public Body getBody() {
		return body;
	}
	
	public void dispose() {
	}
	
	public BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		return bodyDef;
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
			fixtureDef.friction = 1;
			fixtureDef.shape = box;
			fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
			fixtureDef.filter.maskBits = Units.GROUND_FLAG;
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef).setUserData("foot");
		}

		box.dispose();
	}
}
