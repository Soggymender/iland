package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.Camera;
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

    public void setShaderUniforms(Viewport viewport) {

        // Update the projection matrix.
        Matrix4f projectionMatrix = viewport.getSelectedProjectionMatrix();
        shader.setUniform("projectionMatrix", projectionMatrix);

        shader.setUniform("texture_sampler", 0);

    }

    public void setMeshUniforms(Mesh mesh) {
        shader.setUniform("material", mesh.getMaterial());

    }

    public void setEntityUniforms(Camera camera, Scene scene, Entity entity) {

        Matrix4f viewMatrix = camera.getViewMatrix();

        Matrix4f modelViewMatrix = Transform.buildModelViewMatrix(entity, viewMatrix, false);
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