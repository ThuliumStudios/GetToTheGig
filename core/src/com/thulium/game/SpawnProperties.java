package com.thulium.game;

import com.thulium.entity.Enemy;

public class SpawnProperties {
    public enum SpawnType {
        Enemy,
        Npc,
        Item;
    };
    public String name;
    public SpawnType type;
    public int x, y;
    public float width, height;

    public SpawnType getType() {
        return type;
    }

    public void setType(SpawnType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
