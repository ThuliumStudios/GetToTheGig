package com.thulium.scene;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class TransportPoint extends InteractableSceneObject {
    private String newMap;
    public int x, y;

    public TransportPoint(TextureAtlas atlas, String frame) {
        super(atlas, frame);
    }

    @Override
    public String getText() {
        return "Press ENTER to transport";
    }

    @Override
    public void interact() {

    }
}
