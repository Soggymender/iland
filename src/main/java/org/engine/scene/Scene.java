package org.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.engine.input.Input;
import org.engine.renderer.Camera;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;

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

    private Map<Shader, List<Mesh>> shaderMap;

    /*  Store Shader on material next to texture.
        SceneRenderer no longer infers shader.
        Scene mesh list must sort meshes by material
        When a new material is iterated, set up the material and shader
        When a mesh is about to be rendered, call a shader interface that sets the uniforms.
     */

    /*  TODO: These should not be treated uniquely. Instead, add them to the root hierarchy as Entity, and let the
        scene renderer cast them as needed. */
    private SceneLighting sceneLighting;

    public Scene()
    {
        root = new Entity();

        meshMap = new HashMap<>();
        shaderMap = new HashMap<>();
    }

    public void addEntity(Entity entity) {

        Entity entityRoot = entity.findRoot();
        if (entityRoot != root) {
            entityRoot.setParent(root);
        }
    }

    public Map<Shader, List<Mesh>> getMeshShaders() {
        return shaderMap;
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

            // Map the mesh to entity.
            List<Entity> entityList = meshMap.get(mesh);
            if (entityList == null) {
                entityList = new ArrayList<>();
                meshMap.put(mesh, entityList);
            }

            entityList.add(entity);

            // Map the shader to mesh.
            Shader shader = mesh.getMaterial().getShader();

            List<Mesh> meshList = shaderMap.get(shader);
            if (meshList == null) {
                meshList = new ArrayList<>();
                shaderMap.put(shader, meshList);
            }

            meshList.add(mesh);
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

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    public void setSceneLighting(SceneLighting sceneLighting) {
        this.sceneLighting = sceneLighting;
    };

    public void input(Input input) {
        root.input(input);
    }

    /*  The scene will walk the entity hierarchy directly so that it can check for new meshes and add them
        directly for rendering.
     */
    public void update(float interval) {

        update(interval, root);
    }

    private void update(float interval, Entity entity) {

        entity.update(interval);

        if (entity.getNewMeshFlag()) {// .justRenderable()) {
            entity.setNewMeshFlag(false);

            addEntityMeshes(entity);

        }

        if (entity.children == null) {
            return;
        }

        for (Entity child : entity.children) {
            update(interval, child);
        }
    }
}
