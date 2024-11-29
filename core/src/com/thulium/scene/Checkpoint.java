package com.thulium.scene;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Checkpoint extends InteractableSceneObject {

    public Checkpoint(TextureAtlas atlas) {
        super(atlas, "checkpoint");
    }

    @Override
    public String getText() {
        return "Press X to save";
    }

    @Override
    public void interact() {
        setRegion(getAtlas().findRegion("checkpoint"));
    }
}
