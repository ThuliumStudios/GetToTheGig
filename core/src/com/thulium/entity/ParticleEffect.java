package com.thulium.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.Map;

public class ParticleEffect extends Sprite implements Poolable {
    private Animation<TextureRegion> animation;
    private TextureAtlas atlas;
    private float stateTime;

    private static final Map<String, Float> SPEEDS = Map.of(
            "cloud", .075f,
            "hit0", .025f
    );

    public ParticleEffect(TextureAtlas atlas, String name) {
        this.atlas = atlas;

        animation = new Animation<>(.075f, atlas.findRegions(name));
        setSize(.5f, .5f);
        setColor(1, 1, 1, .1f);
        setRegion(animation.getKeyFrame(0));
    }

    public void initialize(String name, float w, float h, float a) {
        stateTime = 0;
        setSize(w, h);
        setColor(1, 1, 1, a);
        animation = new Animation<>(SPEEDS.get(name), atlas.findRegions(name));
        setRegion(animation.getKeyFrame(0));
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);

        setRegion(animation.getKeyFrame(stateTime));
        stateTime += Gdx.graphics.getDeltaTime();
    }

    public boolean isFinished() {
        return animation.getKeyFrameIndex(stateTime) == animation.getKeyFrames().length - 1;
    }

    @Override
    public void reset() {
        stateTime = 0;

    }
}
