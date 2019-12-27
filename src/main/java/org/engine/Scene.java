package org.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.engine.renderer.Mesh;
import org.engine.renderer.Skybox;

public class Scene {

    private Map<Mesh, List<Entity>> meshMap;

    private Skybox skybox;

    private SceneLighting sceneLighting;

    public Scene() {
        meshMap = new HashMap();
    }

    public Map<Mesh, List<Entity>> getEntityMeshes() {
        return meshMap;
    }

    public void setEntities(Entity[] entities) {

        int numEntities = entities != null ? entities.length : 0;

        for (int i = 0; i < numEntities; i++) {
            Entity entity = entities[i];
            Mesh mesh = entity.getMesh();
            List<Entity> list = meshMap.get(mesh);
            if (list == null) {
                list = new ArrayList<>();
                meshMap.put(mesh, list);
            }

            list.add(entity);
        }
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    public void setSceneLighting(SceneLighting sceneLighting) {
        this.sceneLighting = sceneLighting;
    }
}
