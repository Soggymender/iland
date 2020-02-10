package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.renderer.Viewport;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.Matrix4f;

public class SketchUniformManager implements IUniformManager {

    private Shader shader;

    public SketchUniformManager(Shader shader) throws Exception {
        this.shader = shader;


        shader.createUniform("projectionMatrix");
        shader.createUniform("modelViewMatrix");
        shader.createUniform("texture_sampler");

        shader.createMaterialUniform("material");
    }

    public void setShaderUniforms(Transform transform, Viewport viewport) {

        // Update the projection matrix.
        Matrix4f projectionMatrix = viewport.getProjectionMatrix();
        shader.setUniform("projectionMatrix", projectionMatrix);

        shader.setUniform("texture_sampler", 0);

    }

    public void setMeshUniforms(Mesh mesh, Transform transform) {
        shader.setUniform("material", mesh.getMaterial());

    }

    public void setEntityUniforms(Scene scene, Entity entity, Transform transform) {

        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        Matrix4f modelViewMatrix = transform.buildModelViewMatrix(entity, viewMatrix);
        shader.setUniform("modelViewMatrix", modelViewMatrix);

    }

    public boolean getUseSceneLighting() {
        return false;
    }

    public boolean getUseModelViewMatrix() {
        return true;
    }

    public boolean getUseDepthTest() {
        return false;
    }
}