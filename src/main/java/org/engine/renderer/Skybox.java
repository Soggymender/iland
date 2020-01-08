package org.engine.renderer;

import org.engine.Entity;
import org.engine.resources.ResourceLoader;

public class Skybox extends Entity {

    public Skybox(String modelFilename, String texturesDir) throws Exception {
        super();

        // Export the skybox as FBX and include its own material and texture.
        Mesh[] skyboxMesh = ResourceLoader.loadMesh(modelFilename, texturesDir);

        setMesh(skyboxMesh[0]);
        setPosition(0, 0, 0);
    }
}
