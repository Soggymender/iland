package org.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.input.Input;
import org.engine.renderer.Camera;
import org.engine.renderer.Mesh;
import org.engine.renderer.Shader;

public class Scene {

    int count = 0;
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

    private List<Entity> frameEntities;

    public Scene()
    {
        root = new Entity();

        meshMap = new HashMap<>();
        shaderMap = new HashMap<>();

        frameEntities = new ArrayList<>();
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
        //addEntity(camera);

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

        frameEntities.clear();

        update(interval, root);

        collide();

        // The camera has to go first or last, because collision may cause a 2nd position update on the target.
        camera.update(interval);
    }

    private void update(float interval, Entity entity) {

        entity.update(interval);

        frameEntities.add(entity);

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

    private void collide() {

        for (int i = 0; i < frameEntities.size(); i++) {

            Entity a = frameEntities.get(i);
            Vector3f aPos = a.getPosition();
            BoundingBox aBox = a.getBBox();
            

            for (int j = i + 1; j < frameEntities.size(); j++) {

                Entity b = frameEntities.get(j);
                Vector3f bPos = b.getPosition();
                BoundingBox bBox = b.getBBox();
                
                if (!a.flags.collidable ||!a.flags.visible || !b.flags.collidable || !b.flags.visible) {
                    continue;
                }

                if (!a.flags.dynamic && !b.flags.dynamic) {
                    // Nothing is dynamic, nothing to do.
                    continue;
                }

                // Colliding now.
                if ((aPos.x + aBox.min.x <= bPos.x + bBox.max.x && aPos.x + aBox.max.x >= bPos.x + bBox.min.x) &&
                    (aPos.y + aBox.min.y <= bPos.y + bBox.max.y && aPos.y + aBox.max.y >= bPos.y + bBox.min.y)) {
              
                    Vector3f aPrevPos = new Vector3f(aPos);
                    if (a.flags.dynamic) {
                        Vector3f aVel = new Vector3f(a.frameVelocity);
                        aVel.x = -aVel.x;
                        aVel.y = -aVel.y;
                        aVel.z = -aVel.z;
                        aPrevPos.add(aVel);          
                    }
                    
                    Vector3f bPrevPos = new Vector3f(bPos);
                    if (b.flags.dynamic) {
                        Vector3f bVel = new Vector3f(b.frameVelocity);
                        bVel.x = -bVel.x;
                        bVel.y = -bVel.y;
                        bVel.z = -bVel.z;
                        bPrevPos.add(bVel);          
                    }

                    if (a.flags.platform_collision || b.flags.platform_collision) {
                                    
                        // Not colliding previously.
                        if ((aPrevPos.x + aBox.min.x <= bPrevPos.x + bBox.max.x && aPrevPos.x + aBox.max.x >= bPrevPos.x + bBox.min.x) &&
                            !(aPrevPos.y + aBox.min.y < bPrevPos.y + bBox.max.y && aPrevPos.y + aBox.max.y > bPrevPos.y + bBox.min.y)) {


                            // TODO: bbox specifies 2D or 3D, and auto check 3D if applicable.
                            
                            // TODO: if both are dynamic, use some weight factor to figure out how far each resolution vector is scaled.
                            if (a.flags.dynamic) {

                                Vector3f aVel = a.frameVelocity;
                                Vector3f aRes = new Vector3f();

                                if (aVel.y < 0) {
                                    aRes.y = (bPos.y + bBox.max.y) - (aPos.y + aBox.min.y);
                                }

                                a.onCollide(b, aRes);
                            }
                        }
                    } else if (a.flags.box_collision || b.flags.box_collision) {

                          // TODO: if both are dynamic, use some weight factor to figure out how far each resolution vector is scaled.
                          if (a.flags.dynamic) {

                            boolean fromLeft  = aPrevPos.x + aBox.max.x <= bPrevPos.x + bBox.min.x;
                            boolean fromRight = aPrevPos.x + aBox.min.x >= bPrevPos.x + bBox.max.x; 
                            
                            boolean fromTop = aPrevPos.y + aBox.min.y >= bPrevPos.y + bBox.max.y;
                            boolean fromBottom = aPrevPos.y + aBox.max.y <= bPrevPos.y + bBox.min.y;

                            Vector3f aVel = a.frameVelocity;
                            Vector3f aRes = new Vector3f();

                            if (fromLeft) {
                                aRes.x = (bPos.x + bBox.min.x) - (aPos.x + aBox.max.x);
                                count++;
                            }

                            if (fromRight) {
                                aRes.x = (bPos.x + bBox.max.x) - (aPos.x + aBox.min.x);
                            }

                            if (fromTop) {
                                aRes.y = (bPos.y + bBox.max.y) - (aPos.y + aBox.min.y);
                            }

                            a.onCollide(b, aRes);
                        }
                    }
                }
            }
        }
    }
}
