package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.thulium.util.Units;

public class AnimationWrapper {
    private Priority priority;
    private Animation<TextureRegion> animation;
    private TextureAtlas atlas;
    private String name;
    private float speed;
    private float stateTime;

    public AnimationWrapper(String name, float speed, TextureAtlas atlas, Priority priority) {
        this.priority = priority;
        this.atlas = atlas;
        this.speed = speed;
        this.name = name;

        animation = new Animation<>(speed, atlas.findRegions(name));
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void start() {
        stateTime = 0;
    }

    public TextureRegion get() {
        return animation.getKeyFrame(stateTime, Units.isLooping(name));
    }

    public String getName() {
        return name;
    }

    public Priority getPriority() {
        return priority;
    }

    public int getCurrentFrame() {
        // return animation.getKeyFrameIndex(stateTime);
        return MathUtils.floor(stateTime / speed) % (animation.getKeyFrames().length);
    }

    public boolean isFinished() {
       //return animation.getKeyFrameIndex(stateTime) == animation.getKeyFrames().length - 1;
        return animation.isAnimationFinished(stateTime);
    }

    public boolean isLooping() {
        return Units.isLooping(name);
    }
}