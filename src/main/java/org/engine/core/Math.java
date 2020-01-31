package org.engine.core;

import org.joml.Vector3f;

public class Math {

    public static Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f);
    public static Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    public static Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);

    public static float toRadians(float x) {
        return (float) java.lang.Math.toRadians(x);
    }

    public static float toDegrees(float x) {
        return (float) java.lang.Math.toDegrees(x);
    }

    public static float cos(float x) {
        return (float) java.lang.Math.cos(x);
    }

    public static float sin(float x) {
        return (float) java.lang.Math.sin(x);
    }

    public static Vector3f lerp(Vector3f a, Vector3f b, float t) {
        // a + (b - a) * t;
        Vector3f result = b.sub(a);
        result.mul(t);
        return a.add(result);
    }

    public static Vector3f nlerp(Vector3f a, Vector3f b, float t) {
        Vector3f result = lerp(a, b, t);
        return result.normalize();
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float easeIn(float t) {
        return t * t;
    }

    public static float flip(float x) {
        return 1.0f - x;
    }

    public static float easeOut(float t) {

        float result = (float)java.lang.Math.sqrt(flip(t));
        return flip(result);
    }

    public static float easeInOut(float t) {
        float in = easeIn(t);
        float out = easeOut(t);

        return lerp(in, out, t);
    }
}
