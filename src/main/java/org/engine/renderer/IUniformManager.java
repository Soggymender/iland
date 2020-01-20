package org.engine.renderer;

import org.engine.renderer.Shader;
import org.engine.renderer.Mesh;
import org.engine.core.Transform;
import org.engine.scene.Entity;

public interface IUniformManager {

    public void setShaderUniforms(Transform transform);
    public void setMeshUniforms(Mesh mesh, Transform transform);
    public void setEntityUniforms(Entity entity, Transform transform);

    public boolean getUseSceneLighting();
    public boolean getUseModelViewMatrix();
}