package com.thulium.scene;

import com.badlogic.gdx.graphics.g2d.*;
import com.thulium.entity.AnimationWrapper;

public abstract class InteractableSceneObject extends Sprite {
    private Animation<TextureRegion> animation;
    private TextureAtlas atlas;
    private float stateTime;

    public InteractableSceneObject(TextureAtlas atlas, String frame) {
        super(atlas.findRegion(frame));
        this.atlas = atlas;
    }

    public void render(Batch batch) {
        draw(batch);
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public abstract String getText();

    public abstract void interact();
}
