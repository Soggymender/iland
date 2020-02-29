package org.engine.scene;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import org.engine.input.Input;
import org.engine.renderer.Mesh;
import org.engine.core.BoundingBox;

public class Entity {

    public class Flags {
        public boolean dynamic = false;
        public boolean collidable = false;
        public boolean platform_collision = false;
        public boolean box_collision = false;
        public boolean hasBoundingBox = false;

        public boolean newMesh = false;
        public boolean renderable = false;
        public boolean visible = true;
        public boolean parentVisible = true;

        protected Flags() {

        }

        // Copy constructor for copying flags to oldFlags.
        protected Flags(Flags flags) {
            this.newMesh = flags.newMesh;
            this.renderable = flags.renderable;
            this.visible = flags.visible;
            this.parentVisible = flags.parentVisible;
        }
    }

    private Mesh[] meshes;

    protected Vector3f position;
    protected Vector3f rotation;

    protected Vector3f scale;

    protected Vector3f velocity;
    public Vector3f frameVelocity;
    public Vector3f resolutionVec;
    public int numCollisions;

    protected BoundingBox bBox;

    public Entity parent;
    public List<Entity> children = null;

    public Entity support = null;

    // oldFlags should be updated each frame. Differences between the two sets indicate change caused by the update.
    public Flags oldFlags = null;
    public Flags flags = null;

    public Entity() {

        flags = new Flags();
        oldFlags = new Flags();

        position = new Vector3f(0, 0, 0);
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        rotation = new Vector3f(0, 0, 0);

        velocity = new Vector3f();
        frameVelocity = new Vector3f(0, 0, 0);
        resolutionVec = new Vector3f(0, 0, 0);

        bBox = new BoundingBox();
    }

    public Entity(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};

        flags.renderable = true;
        flags.newMesh = true;
    }

    public Entity(Mesh[] meshes) {
        this();
        this.meshes = meshes;

        flags.renderable = true;
        flags.newMesh = true;

        calculateBoundingBox();
    }

    public void setParent(Entity parent) {

        if (this.parent == null) {
            this.parent = parent;

            this.parent.addChild(this);
        } else {
            // If there is already a parent, adopt.
            // Consider how this may impact the scene update.
//            Entity oldParent = this.parent;
//            this.parent.removeChild(this);
        }
    }

    public void addChild(Entity child) {

        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(child);
    }

    public void removeChild(Entity child) {
        if (children == null) {
            return;
        }

        if (children.contains(child)) {
            children.remove(child);
        }
    }

    // Walk the ancestory and return the root back up the call stack.
    public Entity findRoot() {

        if (parent == null) {
            return this;
        }

        return parent.findRoot();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public Vector3f getScale() {

        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public void setScale(float scale) {
        this.scale.set(scale);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public void setVelocity(float x, float y, float z) {
        velocity.x = x;
        velocity.y = y;
        velocity.z = z;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Mesh getMesh() {

        if (meshes == null) {
            return null;
        }

        return meshes[0];
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;

        flags.renderable = true;
        flags.newMesh = true;

        calculateBoundingBox();
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{ mesh };
        flags.renderable = true;
        flags.newMesh = true;

        calculateBoundingBox();
    }

    public void clearMeshes() {
        this.meshes = null;
        flags.renderable = false;
    }

    private void calculateBoundingBox() {

        for (Mesh mesh : meshes) {

            BoundingBox meshBBox = mesh.getBbox();
            if (meshBBox.min.x < bBox.min.x) {
                bBox.min.x = meshBBox.min.x;
            }

            if (meshBBox.min.y < bBox.min.y) {
                bBox.min.y = meshBBox.min.y;
            }
        
            if (meshBBox.max.x > bBox.max.x) {
                bBox.max.x = meshBBox.max.x;
            }

            if (meshBBox.max.y > bBox.max.y) {
                bBox.max.y = meshBBox.max.y;
            }

            flags.hasBoundingBox = true;
        }
    }

    public BoundingBox getBBox() {
        return bBox;
    }

    public boolean justRenderable() {
        return (flags.renderable && !oldFlags.renderable);
    }

    public boolean getNewMeshFlag() {
        return flags.newMesh;
    }

    public void setNewMeshFlag(boolean value) {
        flags.newMesh = value;
    }

    public boolean getVisible() {
        return flags.visible;
    }

    public void setVisible(boolean value) {
        flags.visible = value;
    }

    public boolean getParentVisible() {
        return flags.parentVisible;
    }

    public void setParentVisible(boolean value) {
        flags.parentVisible = value;
    }

    public void input(Input input) {

        if (children == null) {
            return;
        }

        for (Entity child : children) {
            child.input(input);
        }
    }

    public void update(float interval) {

        // NOTE: Some things need to be updated whether dirty or not because ancestors affect some state data.

        numCollisions = 0;
        resolutionVec.zero();

        frameVelocity.x = velocity.x * interval;
        frameVelocity.y = velocity.y * interval;
        frameVelocity.z = velocity.z * interval; 

        // Apply velocity to position.
        //position.add(frameVelocity);
        
        oldFlags = new Flags(flags);

        // If parent or grandparent is not visible, set not visible.
        if (parent == null) {
            setParentVisible(true);
        } else {
            setParentVisible(parent.getVisible() && parent.getParentVisible());
        }

        support = null;
    }

    public void Shutdown() {
        int numMeshes = this.meshes != null ? this.meshes.length : 0;
        for (int i = 0; i < numMeshes; i++) {
            this.meshes[i].shutdown();
        }
    }

    public void onCollide(Entity other, Vector3f resolutionVec) {

        this.resolutionVec.add(resolutionVec);
        numCollisions++;

        /*
        frameVelocity.add(resolutionVec);

        //position.add(resolutionVec);

        if (resolutionVec.x != 0) {
            velocity.x = 0;
        }
*/
        if (resolutionVec.y != 0) {
  //          velocity.y = 0;

            if (resolutionVec.y > 0) {
                // Resolving upward so this is a supporting surface.
                support = other;
            }
        }
    }
}