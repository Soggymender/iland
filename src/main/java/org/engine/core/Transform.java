package org.engine.core;

import org.engine.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.engine.renderer.Camera;

public class Transform {

    private Matrix4f projectionMatrix;
    private Matrix4f modelMatrix;
    private Matrix4f modelViewMatrix;

    private Matrix4f viewMatrix;
    private Matrix4f orthoMatrix;
    private Matrix4f orthoModelMatrix;

    public Transform() {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();

        viewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
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

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f updateViewMatrix(Camera camera) {

        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();

        viewMatrix.rotate(rotation.x, new Vector3f(1, 0, 0)).rotate(rotation.y, new Vector3f(0, 1, 0));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        return viewMatrix;
    }

    public Matrix4f buildModelViewMatrix(Entity entity, Matrix4f viewMatrix) {
        Vector3f rotation = entity.getRotation();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX(-rotation.x).
                rotateY(-rotation.y).
                rotateZ(-rotation.z).
                scale(entity.getScale());

        modelViewMatrix.set(viewMatrix);

        return modelViewMatrix.mul(modelMatrix);
    }

    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public Matrix4f buildOrthoProjectionModelMatrix(Entity entity, Matrix4f orthoMatrix) {
        Vector3f rotation = entity.getRotation();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX(-rotation.x).
                rotateY(-rotation.y).
                rotateZ(-rotation.z).
                scale(entity.getScale());

        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelMatrix);

        return orthoModelMatrix;
    }
}