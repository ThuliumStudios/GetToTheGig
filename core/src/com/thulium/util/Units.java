package com.thulium.util;

import com.badlogic.gdx.math.Vector2;
import com.thulium.entity.Enemy;

import java.util.List;
import java.util.Map;

public final class Units {
	public static final int WIDTH = 12;
	public static final int HEIGHT = 8;
	public static final int JUMP = 11;
	public static final float GRAVITY = -30;
	public static final float MAX_CHARGE = 1.2f;
	public static final float MAX_VELOCITY = 5;
	public static final float THROW = MAX_VELOCITY * 1.5f;
	public static final short NONE_FLAG = Short.MAX_VALUE;
	public static final short ALL_FLAG = 1; // 1 << 0
	public static final short PLAYER_FLAG = 1 << 1;
	public static final short GROUND_FLAG = 1 << 2;
	public static final short ENTITY_FLAG = 1 << 3;
	public static final Vector2 POWER_SLIDE = new Vector2(1.5f, 0);
	public static final Vector2 ATTACK_FORCE = new Vector2(5, 8);

	public static final List<String> loopedAnimations = List.of("idle", "run", "jump_up", "jump_down", "rare");

	public static boolean isLooping(String animation) {
		return loopedAnimations.contains(animation);
	}
}
