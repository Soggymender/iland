package org.engine.renderer;

import static org.lwjgl.opengl.GL11.*;
import org.joml.Matrix4f;

public class Viewport {

    public float x, y;
    public float width, height;
    public float zNear, zFar;
    public float fov;

    boolean useOrtho = true;
    private Matrix4f selectedProjectionMatrix;

    private Matrix4f perspectiveMatrix;
    private Matrix4f orthoMatrix;

    private boolean centerOrtho = false;

    public Viewport() {

        perspectiveMatrix = new Matrix4f();

        orthoMatrix = new Matrix4f();
    }

    public void set(float x, float y, float width, float height, float zNear, float zFar, float fov) {
        set(x, y, width, height, zNear, zFar, fov, this.centerOrtho);
    }

    public void set(float x, float y, float width, float height, float zNear, float zFar, float fov, boolean centerOrtho) {
       
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zNear = zNear;
        this.zFar = zFar;
        this.fov = fov;

       // centerOrtho = true;
        this.centerOrtho = centerOrtho;

        glViewport((int)x, (int)y, (int)width, (int)height);
    
        updatePerspectiveProjectionMatrix(fov, width, height, zNear, zFar);

        if (centerOrtho) {
            updateOrthoProjectionMatrix(-width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f, zNear, zFar);
        } else {
            updateOrthoProjectionMatrix(0, width, 0, height, zNear, zFar);
        }

        if (useOrtho) {
            selectProjectionMatrix(orthoMatrix);
        } else {
            selectProjectionMatrix(perspectiveMatrix);
        }
    }

    public final Matrix4f updatePerspectiveProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        perspectiveMatrix.identity();
        perspectiveMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return perspectiveMatrix;
    }
    
    public final Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
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