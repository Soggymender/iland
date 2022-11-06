package org.engine.core;

import org.joml.Vector3f;

public class BoundingBox {
    public Vector3f min;
    public Vector3f max;

    public BoundingBox() {
        min = new Vector3f();
        max = new Vector3f();
    }

    public BoundingBox(BoundingBox other) {
        min = new Vector3f(other.min);
        max = new Vector3f(other.max);
    }

    public void reset() {
        min.zero();
        max.zero();
    }
}