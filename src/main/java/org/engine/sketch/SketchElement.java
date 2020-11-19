package org.engine.sketch;

import java.util.List;
import java.util.ArrayList;

import org.engine.renderer.ShaderCache;
import org.joml.Vector4f;

import org.engine.renderer.Color;
import org.engine.renderer.Material;
import org.engine.renderer.Shader;
import org.engine.renderer.Mesh;
import org.engine.scene.Entity;

public class SketchElement extends Entity {

    List<Float> lines = null;

    protected class Flags {
        protected boolean dirty = true; // Everything starts dirty so it'll do an initial update after construction.
    }

    protected Material material;

    protected Flags flags = new Flags();

    public SketchElement(Entity parent) {
        super();

        if (parent != null){
            setParent(parent);
        }
        
        lines = new ArrayList<Float>();

        // Use a new material instance so the color can be modified.
        material = new Material(Color.BLACK, 1.0f);

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultShader = shaderCache.getShader("sketchShader");

        material.setShader(defaultShader);
    }

    public void clear() {
        lines.clear();

        // Should this set flags.dirty = true? yes?
    }

    public void addLines(Vector4f color, Float ...coords) {

        material.setAmbientColor(color);
        material.setDiffuseColor(color);
    
        for (float coord : coords ) {
            lines.add(coord);
        }

        flags.dirty = true;
    }

    @Override
    public void update(float interval) {

       if (!flags.dirty) {
            super.update(interval);
            return;
        }

        super.update(interval);

        flags.dirty = false;
        
        buildMesh();
    }

    private void buildMesh() {

        Mesh mesh = getMesh();

        int posCount = lines.size() / 3;

        float[] positions = new float[posCount * 3];
        float[] texCoords = new float[posCount * 2];
        int[] indices = new int[posCount * 2];

        int indexCount = 0;

        for (int i = 0; i < lines.size(); i += 3 ) {
            positions[i + 0] = lines.get(i + 0);
            positions[i + 1] = lines.get(i + 1);
            positions[i + 2] = lines.get(i + 2);

            // TODO: Maybe use a palette texture for visual effects?
            texCoords[0] = 0.0f;
            texCoords[1] = 0.0f;

            // 2 indices per line. Makes it easy to draw line segments.
            // These are maybe just for debugging, so they don't need to be overly optimized at the moment.
            indices[indexCount] = indexCount++; 
        }

        float[] normals = new float[0];

        if (mesh == null) {
            mesh = new Mesh(Mesh.LINES, positions, texCoords, normals, indices);
            setMesh(mesh);
            mesh.setMaterial(material);
        } else {
            mesh.set(Mesh.LINES, positions, texCoords, normals, indices);
        }
    }
}