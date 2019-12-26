package org.engine.renderer;

import org.engine.Entity;
import org.engine.resources.StaticMeshLoader;

public class Skybox extends Entity {

    public Skybox(String modelFilename, String texturesDir) throws Exception {
        super();

        // Export the skybox as FBX and include its own material and texture.
        Mesh[] skyboxMesh = StaticMeshLoader.load(modelFilename, texturesDir);

        setMesh(skyboxMesh[0]);
        setPosition(0, 0, 0);
    }
}