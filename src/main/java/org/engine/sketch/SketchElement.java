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
    List<Float> lineColors = null;

    protected class Flags {
        protected boolean dirty = true; // Everything starts dirty so it'll do an initial update after construction.
        protected boolean colored = false;
    }

    protected Material material;

    protected Flags flags = new Flags();

    public SketchElement(Entity parent) {
        super();

        if (parent != null){
            setParent(parent);
        }
        
        lines = new ArrayList<Float>();
        lineColors = new ArrayList<Float>();

        // Use a new material instance so the color can be modified.
        material = new Material(Color.BLACK, 1.0f);

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultShader = shaderCache.getShader("sketchShader");

        material.setShader(defaultShader);
    }

    public void clear() {
        lines.clear();
        lineColors.clear();

        // Should this set flags.dirty = true? yes?
    }

    /*
    public void addLines(Vector4f color, Float ...coords) {

        material.setAmbientColor(color);
        material.setDiffuseColor(color);
    
        for (float coord : coords ) {
            lines.add(coord);
        }

        flags.dirty = true;
    }
    */

    // TODO: When adding lines check if they are individually colored. If so, shore up the colors array
    // with the correct number.

    public void addLines(Vector4f color, Float ...coords) {
    
        for (float coord : coords ) {
            lines.add(coord);
        }

        // Each point specifies a 3f color. It will be stored in the mesh as a normal since the usual vertex
        // format does not support per vertex color, and sketch lines do not support normals.
        for (int i = 0; i < coords.length / 3; i++) {
            lineColors.add(color.x);
            lineColors.add(color.y);
            lineColors.add(color.z);
            lineColors.add(color.w);

            // Alpha channel doesn't fit in 3f normal. Use Material alpha, or add a color channel to vertex format.
        }

        flags.dirty = true;
        flags.colored = true;
    }

    public void addLines(Vector4f startColor, Vector4f endColor, Float ...coords) {
    
        for (float coord : coords ) {
            lines.add(coord);
        }

        Color curColor = new Color();
        Color tempColor = new Color();

        // Each point specifies a 3f color. It will be stored in the mesh as a normal since the usual vertex
        // format does not support per vertex color, and sketch lines do not support normals.
        for (int i = 0; i < coords.length / 3; i++) {

            float pct = (float)i / ((coords.length / 3.0f) - 1.0f);

            // Cross fade from start color to end color over the line set.
            curColor.set(startColor);
            curColor.mul(1.0f - pct);
            
            tempColor.set(endColor);
            tempColor.mul(pct);

            curColor.add(tempColor);

            lineColors.add(curColor.x);
            lineColors.add(curColor.y);
            lineColors.add(curColor.z);
            lineColors.add(curColor.w);

            // Alpha channel doesn't fit in 3f normal. Use Material alpha, or add a color channel to vertex format.
        }

        flags.dirty = true;
        flags.colored = true;
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
        float[] colors = new float[posCount * 4];
        float[] texCoords = new float[posCount * 2];
        int[] indices = new int[posCount * 2];

        // Sketch Element may store color data as mesh normals - if colored lines were added.
        float[] normals = new float[0];
        
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

        for (int i = 0; i < lineColors.size(); i++) {
            colors[i] = lineColors.get(i);
        }

        if (mesh == null) {
            mesh = new Mesh(Mesh.LINES, positions, colors, texCoords, normals, indices);
            setMesh(mesh);
            mesh.setMaterial(material);
            //material.setTransparent();
        } else {
            mesh.set(Mesh.LINES, positions, colors, texCoords, normals, indices);
        }
    }
}