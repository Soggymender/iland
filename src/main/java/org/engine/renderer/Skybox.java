package org.engine.renderer;

import org.engine.scene.Entity;
import org.engine.scene.SceneLoader;

public class Skybox extends Entity {

    public Skybox(String modelFilename, String texturesDir) throws Exception {
        super();

        // Export the skybox as FBX and include its own material and texture.
        Mesh[] skyboxMesh = SceneLoader.loadMesh(modelFilename, texturesDir);

        Material material = skyboxMesh[0].getMaterial();

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultSkyboxShader = shaderCache.getShader("defaultSkybox");

        material.setShader(defaultSkyboxShader);

        setMesh(skyboxMesh[0]);
        setPosition(0, 0, 0);
    }
}
