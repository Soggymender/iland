package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.scene.Entity;
import org.engine.scene.SceneRenderer;

public class GuiUniformManager implements IUniformManager {

    private Shader shader;

    public GuiUniformManager(Shader shader) throws Exception {
        this.shader = shader;

        // Create uniforms for Ortographic-model projection matrix and base colour
        shader.createUniform("projModelMatrix");
        shader.createUniform("color");
        shader.createUniform("hasTexture");
    }

    public void setShaderUniforms(Transform transform) {

    }

    public void setMeshUniforms(Mesh mesh, Transform transform) {

    }

    public void setEntityUniforms(Entity entity, Transform transform) {

    }

    public boolean getUseSceneLighting() {
        return false;
    }

    public boolean getUseModelViewMatrix() {
        return false;
    }
}