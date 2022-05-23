package com.thulium.util;

public final class Units {
	public static final int WIDTH = 8;
	public static final int HEIGHT = 5;
	public static final int JUMP = 12;
	public static final float MAX_VELOCITY = 5;
	public static final short ALL_FLAG = 1 << 0;
	public static final short NONE_FLAG = Short.MAX_VALUE;
	public static final short GROUND_FLAG = 1 << 2;
	public static final short ENTITY_FLAG = 1 << 3;

	public static final float AMP_MASS = 0;

	public static final String[] animations = { "idle", "run", "jump_up", "jump_down" };

	public static boolean isLooping(String animation) {
		for (String anims : animations)
			if (animation.equals(anims))
				return true;
		return false;
	}
}
