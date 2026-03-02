package com.thulium.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Jukebox {
    public static Jukebox instance;
    private final Music music;

    private Jukebox() {
        music = Gdx.audio.newMusic(Gdx.files.internal("raw/level_1.ogg"));
        playMusic(1); // TODO: Get based on setting/preference
    }

    public static Jukebox getInstance() {
        if (instance == null)
            instance = new Jukebox();
        return instance;
    }

    public void playMusic(float volume) {
        music.setLooping(true);
        music.setVolume(volume);
        music.play();
    }

    public void playSFX() {
        Sound sfx = Gdx.audio.newSound(Gdx.files.internal("raw/level_1.ogg"));
        sfx.play(music.getVolume());
    }

    public void setVolume(float volume) {
        music.setVolume(volume);
    }

    public void pause() {
        music.pause();
    }

    public void resume() {
        music.play();
    }

    public void stop() {
        music.stop();
    }
}