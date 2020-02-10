package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.renderer.Viewport;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.Matrix4f;

public class SkyboxUniformManager implements IUniformManager {

    private Shader shader;

    public SkyboxUniformManager(Shader shader) throws Exception {
        this.shader = shader;

        // Create uniforms for projection matrix
        shader.createUniform("projectionMatrix");
        shader.createUniform("modelViewMatrix");
        shader.createUniform("texture_sampler");
        shader.createUniform("ambientLight");
    }

    public void setShaderUniforms(Transform transform, Viewport viewport) {

        // Update projection Matrix
        Matrix4f projectionMatrix = viewport.getProjectionMatrix();
        shader.setUniform("projectionMatrix", projectionMatrix);

        shader.setUniform("texture_sampler", 0);
    }

    public void setMeshUniforms(Mesh mesh, Transform transform) {

    }

    public void setEntityUniforms(Scene scene, Entity entity, Transform transform) {
        Matrix4f viewMatrix = new Matrix4f(scene.getCamera().getViewMatrix());
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);

        Matrix4f modelViewMatrix = transform.buildModelViewMatrix(entity, viewMatrix);
        shader.setUniform("modelViewMatrix", modelViewMatrix);
        shader.setUniform("ambientLight", scene.getSceneLighting().getAmbientLight());

    }

    public boolean getUseSceneLighting() {
        return false;
    }

    public boolean getUseModelViewMatrix() {
        return false;
    }

    public boolean getUseDepthTest() {
        return true;
    }
}