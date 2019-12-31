package org.engine.renderer;

import org.joml.Vector3f;

import org.engine.Entity;

public class Camera extends Entity {

    public Camera() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(rotation.y) * -1.0f * offsetZ;
            position.z += (float)Math.cos(rotation.y) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(rotation.y - java.lang.Math.toRadians(90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(rotation.y - java.lang.Math.toRadians(90)) * offsetX;
        }
        position.y += offsetY;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }
}