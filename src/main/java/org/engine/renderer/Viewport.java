package org.engine.renderer;

import org.joml.Matrix4f;

public class Viewport {

    private Matrix4f projectionMatrix;
    private Matrix4f orthoMatrix;


    public Viewport() {

        projectionMatrix = new Matrix4f();

        orthoMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public final Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }
    
    public final Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public final Matrix4f getOrthoProjectionMatrix() {
        return orthoMatrix;
    }   
}