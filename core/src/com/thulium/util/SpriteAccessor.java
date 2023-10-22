package com.thulium.util;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteAccessor implements TweenAccessor<Sprite> {
    public static final int OPACITY = 0;
    @Override
    public int getValues(Sprite target, int tweenType, float[] returnValues) {
        return 0;
    }

    @Override
    public void setValues(Sprite target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case OPACITY:
                target.setAlpha(newValues [0]);
                break;
            default:
                break;
        }
    }
}
