package org.engine.renderer;

import org.engine.renderer.Mesh;
import org.engine.core.Transform;
import org.engine.scene.Entity;
import org.engine.scene.Scene;

public interface IUniformManager {

    public void setShaderUniforms(Transform transform, Viewport viewport);
    public void setMeshUniforms(Mesh mesh, Transform transform);
    public void setEntityUniforms(Scene scene, Entity entity, Transform transform);

    public boolean getUseSceneLighting();
    public boolean getUseModelViewMatrix();
    public boolean getUseDepthTest();
}