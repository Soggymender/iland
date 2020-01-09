package org.engine.scene;

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

    public void addEntity(Entity entity) {

        Mesh[] meshes = entity.getMeshes();
        if (meshes == null) {
            return;
        }

        for (Mesh mesh : meshes) {

            List<Entity> list = meshMap.get(mesh);
            if (list == null) {
                list = new ArrayList<>();
                meshMap.put(mesh, list);
            }

            list.add(entity);
        }
    }

    public void addEntities(Entity[] entities) {

        for (Entity entity : entities) {
            addEntity(entity);
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
