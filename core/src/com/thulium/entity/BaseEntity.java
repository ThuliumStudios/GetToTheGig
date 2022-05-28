package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.thulium.util.Units;

public class BaseEntity extends Sprite{
	private Animation<TextureRegion> animation;
	private TextureAtlas atlas;
	private String animationName = "";

	private boolean flip;
	private boolean looping;
	private float stateTime;

	public BaseEntity(TextureAtlas atlas) {
		super(atlas.getRegions().first());
		this.atlas = atlas;
		setSize(1, 1);
		
		animation = new Animation<TextureRegion>(1f, atlas.findRegions("idle"));
	}

	public void render(Batch batch) {
		setRegion(animation.getKeyFrame(stateTime, looping));
		flip(flip, false);
		draw(batch);
	}

	public void update(float delta) {
		if (animation.isAnimationFinished(stateTime) && !looping)
			animate("idle", 1, true);
		stateTime += delta;
	}
	
	public void animate(String animationName) {
		animate(animationName, .15f);
	}

	public void animate(String animationName, float speed) {
		animate(animationName, speed, Units.isLooping(animationName));
	}
	
	public void animate(String animationName, float speed, boolean looping) {
		if (this.animationName.equals(animationName))
			return;
		
		this.animationName = animationName;
		this.looping = looping;
		stateTime = 0f;

		animation = new Animation<TextureRegion>(speed, atlas.findRegions(animationName));
	}
	
	public String getAnimationName() {
		return animationName;
	}
	
	public void setFlipState(boolean isFlipped) {
		this.flip = isFlipped;
	}

	public float getStateTime() {
		return stateTime;
	}
	
	public boolean isFlipped() {
		return flip;
	}

	public boolean isLooping() {
		return looping;
	}

	public void dispose() {
		
	}
}
