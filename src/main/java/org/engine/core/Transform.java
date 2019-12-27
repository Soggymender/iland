package org.engine.core;

import org.engine.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.engine.renderer.Camera;

public class Transform {

    private Matrix4f projectionMatrix;
    private Matrix4f modelViewMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f orthoMatrix;

    public Transform() {
        projectionMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();

        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0)).rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        return viewMatrix;
    }

    public Matrix4f getModelViewMatrix(Entity entity, Matrix4f viewMatrix) {
        Vector3f rotation = entity.getRotation();

        modelViewMatrix.identity().translate(entity.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(entity.getScale());

        Matrix4f view = new Matrix4f(viewMatrix);

        return view.mul(modelViewMatrix);
    }

    public Matrix4f getOrthoModelMatrix(Entity entity, Matrix4f orthoMatrix) {
        Vector3f rotation = entity.getRotation();

        Matrix4f modelMatrix = new Matrix4f();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(entity.getScale());

        Matrix4f orthoMatrixCur = new Matrix4f(orthoMatrix);
        orthoMatrixCur.mul(modelMatrix);

        return orthoMatrixCur;
    }
}