package com.thulium.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Jukebox {
    private Music music;

    public void Jukebox() {

    }

    public  void playMusic(float volume) {
        music = Gdx.audio.newMusic(Gdx.files.internal("raw/level_1.ogg"));
        music.setLooping(true);
        music.setVolume(volume);
        music.play();
    }

    public void setVolume(float volume) {
        music.setVolume(volume);
    }
}
