package com.thulium.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class CameraHelper {
    float[] samples;
    float internalTimer = 0;
    float shakeDuration = 0;
    private float intensity;

    int duration = 5; // In seconds, make longer if you want more variation
    int frequency = 35; // hertz
    float amplitude = 2; // how much you want to shake

    int sampleCount;

    public CameraHelper() {
        sampleCount = duration * frequency;
        samples = new float[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            samples[i] = MathUtils.random() * 2f - 1f;
        }
        intensity = 2f;
    }

    /**
     * Called every frame will shake the camera if it has a shake duration
     *
     * @param camera your camera
     * @param delta     Gdx.graphics.getDeltaTime() or your dt in seconds
     */
    public void update(OrthographicCamera camera, float x, float y, float delta) {
        internalTimer += delta;
        if (internalTimer > duration)
            internalTimer -= duration;
        if (isShaking()) {
            shakeDuration -= delta;
            float shakeTime = (internalTimer * frequency);
            int first = (int) shakeTime;
            int second = (first + 1) % sampleCount;
            int third = (first + 2) % sampleCount;
            float deltaT = shakeTime - (int) shakeTime;
            float deltaX = samples[first] * deltaT + samples[second] * (1f - deltaT);
            float deltaY = samples[second] * deltaT + samples[third] * (1f - deltaT);

            camera.position.x = x + deltaX * amplitude * (Math.min(shakeDuration, 1f));
            camera.position.y = y + deltaY * amplitude * (Math.min(shakeDuration, 1f));
            camera.update();
        }
    }

    /**
     * Will make the camera shake for the duration passed in in seconds
     *
     * @param d duration of the shake in seconds
     * @param i the intensity of the shake
     */
    public void shake(float d, float i) {
        shakeDuration = d;
        intensity = i;
    }

    public boolean isShaking() {
        return shakeDuration > 0;
    }
}
