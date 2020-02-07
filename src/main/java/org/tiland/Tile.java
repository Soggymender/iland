package org.tiland;

import org.engine.renderer.Color;
import org.engine.renderer.Mesh;
import org.engine.renderer.Material;
import org.engine.renderer.Shader;
import org.engine.renderer.ShaderCache;

public class Tile {

    public enum Type {
        NONE,
        DIRT,
        COUNT
    }

    Type type = Type.DIRT;

    protected Mesh mesh;
    protected Material material;

    public Tile(Color color) throws Exception {

        material = new Material();

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultShader = shaderCache.getShader("default");

        material.setShader(defaultShader);
        material.setAmbientColor(color);

        buildMesh();
    }

    private void buildMesh() {

        float depth = 0.0f;

        float[] positions = new float[4 * 3];
        float[] texCoords = new float[4 * 2];
        int[] indices = new int[6];

        // Top left
        positions[0] = 0.0f;
        positions[1] = 0.0f;
        positions[2] = depth;

        texCoords[0] = 0.0f;
        texCoords[1] = 0.0f;

        // Top right
        positions[3] = 1.0f;
        positions[4] = 0.0f;
        positions[5] = depth;

        texCoords[2] = 1.0f;
        texCoords[3] = 0.0f;

        // Bottom right
        positions[6] = 1.0f;
        positions[7] = 1.0f;
        positions[8] = depth;

        texCoords[4] = 1.0f;
        texCoords[5] = 1.0f;

        // Bottom left
        positions[9] = 0.0f;
        positions[10] = 1.0f;
        positions[11] = depth;

        texCoords[6] = 0.0f;
        texCoords[7] = 1.0f;

        indices[0] = 3;
        indices[1] = 0;
        indices[2] = 1;

        indices[3] = 1;
        indices[4] = 2;
        indices[5] = 3;

        float[] normals = new float[0];

        mesh = new Mesh(Mesh.TRIANGLES, positions, texCoords, normals, indices);
        mesh.setMaterial(material);
    }
}