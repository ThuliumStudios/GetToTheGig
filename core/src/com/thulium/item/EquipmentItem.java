package com.thulium.item;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EquipmentItem extends Item {

    public EquipmentItem(TextureRegion texture, float x, float y, float width, float height) {
        super(texture, x, y, width, height);
    }

    @Override
    public void obtain() {

    }
}
