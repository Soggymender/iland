package org.engine.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.engine.scene.Entity;
import org.engine.renderer.Viewport;

public class Camera extends Entity {

    private static final float FOV = (float)java.lang.Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    private boolean centerOrtho = false;

    Matrix4f viewMatrix;
    Viewport viewport;

    public boolean autoResize = false;

    Window window;

    public Camera(Window window) {

        this();

        this.window = window;

        autoResize = true;
    }

    public Camera() {

        viewport = new Viewport();

        viewMatrix = new Matrix4f();

        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public void setViewport(float x, float y, float width, float height, boolean centerOrtho) {

        viewport.set(x, y, width, height, Z_NEAR, Z_FAR, FOV, centerOrtho);
    }

    public Viewport getViewport() {
        return viewport;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void setViewMatrix(Matrix4f newViewMat){

        viewMatrix.set(newViewMat);
    }

    public void updateViewMatrix() {

        viewMatrix.identity();

        viewMatrix.rotate(rotation.x, new Vector3f(1, 0, 0)).rotate(rotation.y, new Vector3f(0, 1, 0));
        viewMatrix.translate(-position.x, -position.y, -position.z);
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

    public float getFov() {
        return FOV;
    }

    @Override
    public void update(float interval) {

        updateViewMatrix();

        if (autoResize && window.isResized()) {

            float windowWidth = window.getWidth();
            float windowHeight = window.getHeight();

            viewport.set(0, 0, windowWidth, windowHeight, Z_NEAR, Z_FAR, FOV);

          //  window.setResized(false);
        }
    }
}