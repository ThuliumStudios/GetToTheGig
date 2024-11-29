package com.thulium.player;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.thulium.util.Units;

public class PlayerProjectile {
    private Animation<TextureRegion> animation;
    private final TextureAtlas atlas;
    private final Sprite sprite;
    private Body body;

    private boolean isLive;
    private boolean isFlipped;
    private float stateTime;
    private final float spd = .075f;    // TODO: Consider moving to @{Units} class

    public PlayerProjectile(TextureAtlas atlas) {
        this.atlas = atlas;
        sprite = new Sprite();
        sprite.setSize(1.5f, 1.5f); // TODO: Make variable based on player size

        animation = new Animation<>(spd, atlas.findRegions("axe_projectile"));
    }

    public void render(Batch batch, float delta) {
        // Render sprite over body
        sprite.setRegion(animation.getKeyFrame(stateTime, true));
        sprite.setPosition(body.getPosition().x - (sprite.getWidth() / 2), body.getPosition().y - (sprite.getHeight() / 2));
        sprite.flip(!isFlipped, false); // TODO: Why is this inverted from the player?
        sprite.draw(batch);

        stateTime += delta;
    }

    public void animate(String animationName) {
        animation = new Animation<>(spd, atlas.findRegions(animationName));
    }

    public void throwProjectile(float x, float y, boolean isFlipped) {
        if (isLive)
            return;

        this.isFlipped = isFlipped;
        isLive = true;

        // Set CollisionBody data
        body.setTransform(x, y - (sprite.getHeight() / 2), 0);
        body.setLinearVelocity(Units.THROW * (isFlipped ? -1 : 1), 0);
        body.setGravityScale(0);
        body.setUserData(this); // TODO: Clean up. Replaces user data once guitar is thrown again for collision

        // Set sprite data
        sprite.setPosition(body.getPosition().x - (sprite.getWidth() / 2), body.getPosition().y);

        stateTime = MathUtils.random();
        animate("axe_projectile");
    }

    public void collide() {
        isLive = false;
        body.setLinearVelocity(Vector2.Zero);
        body.applyLinearImpulse(new Vector2(MathUtils.random(-10, 10), MathUtils.random(0, 10)), body.getWorldCenter(), false);
        body.setGravityScale(1);
        body.setUserData(""); // TODO: Clean this up. Removes collision when collided
        animate("axe_spin");
    }

    public boolean isInCamera(OrthographicCamera camera) {
        boolean isInFrustum = camera.frustum.sphereInFrustum(sprite.getX(), sprite.getY(), 0, sprite.getWidth());
        if (isLive)
            isLive = isInFrustum;
        return isInFrustum;
    }

    public void createBody(Body body, float width, float height) {
        this.body = body;

        PolygonShape box = new PolygonShape();
        box.setAsBox(width, height, Vector2.Zero, 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.filter.categoryBits = Units.PLAYER_FLAG;
        fixtureDef.filter.maskBits = Units.ENTITY_FLAG;
        fixtureDef.friction = 0;
        fixtureDef.isSensor = true;
        body.setGravityScale(0);
        body.createFixture(fixtureDef).setUserData(this); // TODO: Evaluate whether to use classes or String/int

        box.dispose();
    }

    public BodyDef getBodyDef(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        return bodyDef;
    }
}
