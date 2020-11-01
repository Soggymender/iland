package org.engine.renderer;

import static org.lwjgl.opengl.GL11.*;
import org.joml.Matrix4f;

public class Viewport {

    public float x, y;
    public float width, height;
    public float zNear, zFar;
    public float fov;

    private Matrix4f projectionMatrix;
    private Matrix4f orthoMatrix;


    public Viewport() {

        projectionMatrix = new Matrix4f();

        orthoMatrix = new Matrix4f();
    }

    public void set(float x, float y, float width, float height, float zNear, float zFar, float fov) {
       
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zNear = zNear;
        this.zFar = zFar;
        this.fov = fov;

        glViewport(0, 0, (int)width, (int)height);

        updateProjectionMatrix(fov, width, height, zNear, zFar);
        updateOrthoProjectionMatrix(0, width, height, 0);
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