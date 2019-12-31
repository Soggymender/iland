package org.engine;

import org.joml.Vector3f;
import org.engine.renderer.Mesh;

public class Entity {

    private Mesh[] meshes;

    protected Vector3f position;

    protected Vector3f scale;

    protected Vector3f rotation;

    public Entity() {
        position = new Vector3f(0, 0, 0);
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        rotation = new Vector3f(0, 0, 0);
    }

    public Entity(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public Entity(Mesh[] meshes) {
        this();
        this.meshes = meshes;
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

    public Vector3f getScale()
    {
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

    public Mesh getMesh() {
        return meshes[0];
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{ mesh };
    }

    public void setMesh(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void Shutdown() {
        int numMeshes = this.meshes != null ? this.meshes.length : 0;
        for (int i = 0; i < numMeshes; i++) {
            this.meshes[i].shutdown();
        }
    }
}