package org.engine.renderer;

import org.joml.Vector4f;

public class Color extends Vector4f {

    public static Vector4f BLACK = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    public static Vector4f WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    public static Vector4f GREY  = new Vector4f(1, 1, 1, 0.75f);
                                                
    public Color(float red, float green, float blue, float alpha) {
        x = red;
        y = green;
        z = blue;
        w = alpha;
    }
}