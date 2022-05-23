package com.thulium.main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.thulium.main.MainGame;

public class GetToTheGigDesktop {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.title = "Get to the Gig";
		new LwjglApplication(new MainGame(), config);
	}
}
