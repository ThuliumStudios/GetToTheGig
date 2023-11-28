package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.thulium.util.Units;

import java.util.HashMap;
import java.util.Map;

public class BaseEntity extends Sprite{
	private TextureAtlas atlas;
	private boolean flip;
	private Map<String, AnimationWrapper> animations;
	private AnimationWrapper animation;

	public BaseEntity(TextureAtlas atlas, float width, float height) {
		super(atlas.getRegions().first());
		this.atlas = atlas;
		setSize(width, height);

		animations = new HashMap<>();
		addAnimation("idle", new AnimationWrapper("idle", 1f, atlas, Priority.Bottom));
		animation = animations.get("idle");
	}

	public void render(Batch batch) {
		setRegion(animation.get());
		flip(flip, false);
		draw(batch);
	}

	public void update(float delta) {
		animation.update(delta);
	}
	
	public void animate(String animationName) {
		if (animation.getName().equals(animationName))
			return;

		animation = animations.get(animationName);
		animation.start();
	}
	
	public String getAnimationName() {
		return animation.getName();
	}

	// TODO: Possibly delete
	public void setFlipState(boolean isFlipped) {
		this.flip = isFlipped;
	}

	public boolean isFlipped() {
		return flip;
	}

	public boolean isAnimation(String animationName) {
		return animation.getName().contains(animationName); // TODO: Change to equalsIgnoreCase instead of contains
	}

	public boolean isAnmationFinished() {
		return animation.isFinished();
	}

	public int getCurrentAnimationFrame() {
		return animation.getCurrentFrame();
	}

	public Priority getPriority() {
		return animation.getPriority();
	}

	public void addAnimation(String name, AnimationWrapper anim) {
		animations.put(name, anim);
	}

	public void dispose() {
		
	}
}
