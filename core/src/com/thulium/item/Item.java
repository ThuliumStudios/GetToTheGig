package com.thulium.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Item {
    private Sprite sprite;

    public Item(TextureRegion texture, float x, float y, float width, float height) {
        sprite = new Sprite(texture);
        sprite.setBounds(x, y, width, height);
        sprite.setOriginCenter();
    }

    public abstract void obtain();

    public void render(Batch batch) {
        sprite.draw(batch);
    }

    public Sprite getSprite() {
        return sprite;
    }
}
