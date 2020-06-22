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

public class GuiUniformManager implements IUniformManager {

    private Shader shader;

    public GuiUniformManager(Shader shader) throws Exception {
        this.shader = shader;

        // Create uniforms for Ortographic-model projection matrix and base colour
        shader.createUniform("projModelMatrix");
        shader.createUniform("color");
        shader.createUniform("hasTexture");
    }

    public void setShaderUniforms(Viewport viewport) {

    }

    public void setMeshUniforms(Mesh mesh) {
        shader.setUniform("color", mesh.getMaterial().getDiffuseColor());
        shader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);
    }

    public void setEntityUniforms(Camera camera, Scene scene, Entity entity) {

        Matrix4f ortho = camera.getViewport().getOrthoProjectionMatrix();

        // Set ortohtaphic and model matrix for this HUD item
        Matrix4f projModelMatrix = Transform.buildOrthoProjectionModelMatrix(entity, ortho);
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