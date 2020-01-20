package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.scene.Entity;
import org.engine.scene.SceneRenderer;

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