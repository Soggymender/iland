package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.renderer.Viewport;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.Matrix4f;

public class GuiUniformManager implements IUniformManager {

    private Shader shader;

    public GuiUniformManager(Shader shader) throws Exception {
        this.shader = shader;

        // Create uniforms for Ortographic-model projection matrix and base colour
        shader.createUniform("projModelMatrix");
        shader.createUniform("color");
        shader.createUniform("hasTexture");
    }

    public void setShaderUniforms(Transform transform, Viewport viewport) {

    }

    public void setMeshUniforms(Mesh mesh, Transform transform) {
        shader.setUniform("color", mesh.getMaterial().getDiffuseColor());
        shader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);
    }

    public void setEntityUniforms(Scene scene, Entity entity, Transform transform) {

        Matrix4f ortho = scene.getCamera().getViewport().getOrthoProjectionMatrix();

        // Set ortohtaphic and model matrix for this HUD item
        Matrix4f projModelMatrix = transform.buildOrthoProjectionModelMatrix(entity, ortho);
        shader.setUniform("projModelMatrix", projModelMatrix);
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