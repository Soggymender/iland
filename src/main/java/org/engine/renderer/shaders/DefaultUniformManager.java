package org.engine.renderer.shaders;

import org.engine.core.Transform;
import org.engine.renderer.Camera;
import org.engine.renderer.IUniformManager;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;
import org.engine.renderer.Viewport;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneRenderer;
import org.joml.Matrix4f;

public class DefaultUniformManager implements IUniformManager {

    private Shader shader;

    public DefaultUniformManager(Shader shader) throws Exception {
        this.shader = shader;

        shader.createUniform("projectionMatrix");
        shader.createUniform("modelViewMatrix");
        shader.createUniform("texture_sampler");

        shader.createMaterialUniform("material");

        shader.createUniform("specularPower");
        shader.createUniform("ambientLight");

        shader.createPointLightListUniform("pointLights", SceneRenderer.MAX_POINT_LIGHTS);
        shader.createSpotLightListUniform("spotLights", SceneRenderer.MAX_SPOT_LIGHTS);
        shader.createDirectionalLightUniform("directionalLight");
    }

    public void setShaderUniforms(Viewport viewport) {

        // Update the projection matrix.
        Matrix4f projectionMatrix = viewport.getProjectionMatrix();
        shader.setUniform("projectionMatrix", projectionMatrix);

        shader.setUniform("texture_sampler", 0);
    }

    public void setMeshUniforms(Mesh mesh) {

        shader.setUniform("material", mesh.getMaterial());
    }

    public void setEntityUniforms(Camera camera, Scene scene, Entity entity) {

        Matrix4f viewMatrix = camera.getViewMatrix();

        Matrix4f modelViewMatrix = Transform.buildModelViewMatrix(entity, viewMatrix);
        shader.setUniform("modelViewMatrix", modelViewMatrix);
    }

    public boolean getUseSceneLighting() {
        return true;
    }

    public boolean getUseModelViewMatrix() {
        return true;
    }

    public boolean getUseDepthTest() {
        return true;
    }
}
