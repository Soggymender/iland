package org.engine.renderer;

import org.engine.renderer.Mesh;
import org.engine.scene.Entity;
import org.engine.scene.Scene;

public interface IUniformManager {

    public void setShaderUniforms(Viewport viewport);
    public void setMeshUniforms(Mesh mesh);
    public void setEntityUniforms(Camera camera, Scene scene, Entity entity);

    public boolean getUseSceneLighting();
    public boolean getUseModelViewMatrix();
    public boolean getUseDepthTest();
}