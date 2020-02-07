package org.engine.anim.info;

import org.joml.Vector3d;

public class BoneKeyframe {
    float time;
    Vector3d position;
    Vector3d rotation;

    BoneKeyframe(float t, Vector3d p, Vector3d r) {
        time = t;
        position = p;
        rotation = r;
    }
}