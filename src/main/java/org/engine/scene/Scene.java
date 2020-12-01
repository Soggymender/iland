package org.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.engine.input.Input;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;

public class Scene {

  //  int count = 0;
    
    /*  This is the scene root. All entities or entity hierarchies in the scene have this as their root ancestor.
        When an entity is part of this hierarchy, its input and update methods are called automatically. Flag behavior
        can be used to bypass work in those calls */
//    Entity root = null;
    
    // Entities in the scene.
    public List<Entity> entities = null;

    // Entities being rendered.
    private List<Entity> renderEntities = null;

    // Entities processed this frame.
    private List<Entity> frameEntities = null;

    // Entities that need to change parents.
    private List<Entity> adoptions = null;

    /*  A "flat" map of meshes in the scene, where each references the list of entities that reference the mesh.
        TODO: Entities in the root hierarchy will automatically be added to the map. Entities do not have to be in the
        root hierarchy in order to be added manually. */
    private Map<Mesh, List<Entity>> meshToEntityMap;

    private Map<Shader, List<Mesh>> shaderToMeshMap;

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
        //root = new Entity();
        entities = new ArrayList<>();

        renderEntities = new ArrayList<>();
        frameEntities = new ArrayList<>();

        adoptions = new ArrayList<>();

        meshToEntityMap = new HashMap<>();
        shaderToMeshMap = new HashMap<>();
    }

    public Entity findEntity(String name) {

        if (name == null || name.length() == 0) {
            return null;
        }

        Entity result = null;

        for (int i = 0; i < entities.size(); i++) {
            result = findEntity(entities.get(i), name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Entity findEntity(Entity entity, String name) {

        if (entity.getName().equals(name)) {
            return entity;
        }

        Entity result = null;

        if (entity.children == null) {
            return null;
        }
        
        for (Entity child : entity.children) {
            result = findEntity(child, name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public void addEntity(Entity entity) {

        if (!entities.contains(entity)) {
            entities.add(entity);
        }
    }

    // entityOnly is a flag for when we know we are just changing the entity's position within the scene.
    public void removeEntity(Entity entity, boolean entityOnly) {
        if (entities.contains(entity)) {
            entities.remove(entity);
        }

        if (entityOnly) {
            return;
        }
        
        // If the entity being removed is a child it will not be in the entity list, but its meshes may
        // be in the mesh list.

        // Remove meshes if no other entities reference them.
        removeEntityMeshes(entity);
            
        for (Entity child : entity.children) {
            removeEntityMeshes(child);
        }
    }

    public Map<Shader, List<Mesh>> getMeshShaders() {
        return shaderToMeshMap;
    }

    public Map<Mesh, List<Entity>> getEntityMeshes() {
        return meshToEntityMap;
    }

    private void addEntityMeshes(Entity entity) {

        Mesh[] meshes = entity.getMeshes();
        if (meshes == null) {
            return;
        }

        renderEntities.add(entity);

        for (Mesh mesh : meshes) {

            // Map the mesh to entity.
            List<Entity> entityList = meshToEntityMap.get(mesh);
            if (entityList == null) {
                entityList = new ArrayList<>();
                meshToEntityMap.put(mesh, entityList);
            }

            entityList.add(entity);

            // Map the shader to mesh.
            Shader shader = mesh.getMaterial().getShader();

            List<Mesh> meshList = shaderToMeshMap.get(shader);
            if (meshList == null) {
                meshList = new ArrayList<>();
                shaderToMeshMap.put(shader, meshList);
            }

            meshList.add(mesh);
        }
    }

    private void removeEntityMeshes(Entity entity) {

        Mesh[] meshes = entity.getMeshes();
        if (meshes == null) {
            return;
        }

        renderEntities.remove(entity);

        for (Mesh mesh : meshes) {

            // Get the entities for this mesh.
            List<Entity> entityList = meshToEntityMap.get(mesh);
            if (entityList == null) {
                continue;
            }

            if (!entityList.contains(entity)) {
                continue;
            }

            entityList.remove(entity);

            if (entityList.size() > 0) {
                continue;
            }
            
            meshToEntityMap.remove(mesh);

            // Remove the mesh shader if it is unused.

            Shader shader = mesh.getMaterial().getShader();

            List<Mesh> meshList = shaderToMeshMap.get(shader);

            meshList.remove(mesh);

            if (meshList.size() > 0) {
                continue;
            }

            shaderToMeshMap.remove(shader);
        }
    }

    private void addEntitiesMeshes(Entity[] entities) {

        for (Entity entity : entities) {
            addEntityMeshes(entity);
        }
    }

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    public void setSceneLighting(SceneLighting sceneLighting) {
        this.sceneLighting = sceneLighting;
    };

    public List<Entity> getFrameEntities() {
        return frameEntities;
    }

    public void clear() {

        /*
        root = new Entity();

        meshMap.clear();
        shaderMap.clear();
        frameEntities.clear();
        */
    }

    public void input(Input input) {
        for (Entity entity : entities) {
            entity.input(input);
        }
    }

    /*  The scene will walk the entity hierarchy directly so that it can check for new meshes and add them
        directly for rendering.
     */
    public void update(float interval) {

        frameEntities.clear();
        adoptions.clear();

        for (Entity entity : entities) {
            update(interval, entity);
        }

        // Adoptions (parentage changes) have to be handled outside of the update loop since it changes
        // the entities list.
        for (Entity entity : adoptions) {

            // If the current parent is null, remove it from the scene.
            if (entity.parent == null) {
                removeEntity(entity, true);
            }

            entity.setParent(entity.requestedParent);
            entity.requestedParent = null;
            entity.requestedParentValid = false;

            // Add the entity at it's new position.
            if (entity.parent == null) {
                addEntity(entity);
            }
        }
    }

    private void update(float interval, Entity entity) {

        entity.update(interval);

        if (entity.requestedParentValid) {
            adoptions.add(entity);
        }

        frameEntities.add(entity);

        if (!renderEntities.contains(entity)) {

                //if (entity.getNewMeshFlag()) {
                //       entity.setNewMeshFlag(false);

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
