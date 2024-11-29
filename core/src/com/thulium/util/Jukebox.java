package com.thulium.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Jukebox {
    private static Music music;

    public static void playMusic(float volume) {
        music = Gdx.audio.newMusic(Gdx.files.internal("raw/level_1.ogg"));
        music.setLooping(true);
        music.setVolume(volume);
        music.play();
    }

    public static void playSFX() {
        Sound sfx = Gdx.audio.newSound(Gdx.files.internal("raw/level_1.ogg"));
        sfx.play(music.getVolume());
    }

    public static void setVolume(float volume) {
        music.setVolume(volume);
    }
}
