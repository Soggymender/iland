package org.engine.core;

import org.joml.Vector3f;

public class BoundingBox {
    public Vector3f min;
    public Vector3f max;

    public BoundingBox() {
        min = new Vector3f();
        max = new Vector3f();
    }

    public void reset() {
        min.zero();
        max.zero();
    }
}