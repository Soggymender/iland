package org.engine.renderer;

import static org.lwjgl.opengl.GL11.*;
import org.joml.Matrix4f;

public class Viewport {

    public float x, y;
    public float width, height;
    public float zNear, zFar;
    public float fov;

    private Matrix4f selectedProjectionMatrix;

    private Matrix4f perspectiveMatrix;
    private Matrix4f orthoMatrix;


    public Viewport() {

        perspectiveMatrix = new Matrix4f();

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

        updatePerspectiveProjectionMatrix(fov, width, height, zNear, zFar);
        updateOrthoProjectionMatrix(0, width, height, 0);

        selectProjectionMatrix(perspectiveMatrix);
    }

    public final Matrix4f updatePerspectiveProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        perspectiveMatrix.identity();
        perspectiveMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return perspectiveMatrix;
    }
    
    public final Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public void selectProjectionMatrix(Matrix4f matrix) {
        selectedProjectionMatrix = matrix;
    }

    public Matrix4f getSelectedProjectionMatrix() {
        return selectedProjectionMatrix;
    }

    public Matrix4f getPerspectiveProjectionMatrix() {
        return perspectiveMatrix;
    }

    public final Matrix4f getOrthoProjectionMatrix() {
        return orthoMatrix;
    }   
}