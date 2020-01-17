package org.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.engine.input.Mouse;
import org.engine.renderer.Mesh;
import org.engine.renderer.Skybox;

public class Scene {

    /*  This is the scene root. All entities or entity hierarchies in the scene have this as their root ancestor.
        When an entity is part of this hierarchy, its input and update methods are called automatically. Flag behavior
        can be used to bypass work in those calls */
    Entity root = null;

    /*  A "flat" map of meshes in the scene, where each references the list of entities that reference the mesh.
        TODO: Entities in the root hierarchy will automatically be added to the map. Entities do not have to be in the
        root hierarchy in order to be added manually. */
    private Map<Mesh, List<Entity>> meshMap;

    /*  TODO: These should not be treated uniquely. Instead, add them to the root hierarchy as Entity, and let the
        scene renderer cast them as needed. */
    private Skybox skybox;
    private SceneLighting sceneLighting;

    public Scene()
    {
        root = new Entity();

        meshMap = new HashMap();
    }

    public void addEntity(Entity entity) {

        Entity entityRoot = entity.findRoot();
        if (entityRoot != root) {
            entityRoot.setParent(root);
        }
    }

    public Map<Mesh, List<Entity>> getEntityMeshes() {
        return meshMap;
    }

    public void addEntityMeshes(Entity entity) {

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

    public void addEntitiesMeshes(Entity[] entities) {

        for (Entity entity : entities) {
            addEntityMeshes(entity);
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

    public void input(Mouse mouse, float interval) {
        root.input(mouse, interval);
    }

    public void update(float interval) {
        root.update(interval);
    }
}
