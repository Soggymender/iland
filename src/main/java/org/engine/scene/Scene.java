package org.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.engine.IHud;
import org.engine.input.Input;
import org.engine.renderer.Camera;
import org.engine.renderer.Mesh;
import org.engine.renderer.Skybox;

public class Scene {

    Camera camera = null;

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
    private IHud hud;

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

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {

        // Add the camera to the scene so it can update automatically.
        addEntity(camera);

        // Reference the active camera directly for various view related processes.
        this.camera = camera;
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
    };

    public IHud getHud() {
        return hud;
    }

    public void setHud(IHud hud) {
        this.hud = hud;
    }

    public void input(Input input) {
        root.input(input);
    }

    public void update(float interval) {
        root.update(interval);
    }
}
