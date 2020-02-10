package org.engine.core;

import org.engine.scene.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {

    private Matrix4f modelMatrix;
    private Matrix4f modelViewMatrix;

    private Matrix4f orthoModelMatrix;

    public Transform() {
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();

        orthoModelMatrix = new Matrix4f();
    }

    public Matrix4f buildModelViewMatrix(Entity entity, Matrix4f viewMatrix) {
        Vector3f rotation = entity.getRotation();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX(-rotation.x).
                rotateY(-rotation.y).
                rotateZ(-rotation.z).
                scale(entity.getScale());

        modelViewMatrix.set(viewMatrix);
        modelViewMatrix.mul(modelMatrix);

        return modelViewMatrix;
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

    public Vector3f unproject(Vector3f point) {

        Vector3f result = new Vector3f();

        return result;
    }
}