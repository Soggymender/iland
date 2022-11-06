package org.engine.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.engine.core.BoundingBox;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryUtil;

import org.engine.scene.Entity; // I don't want this here.

import org.joml.Vector3f;

public class Mesh {

    public static final int
        LINES          = GL_LINES,
        TRIANGLES      = GL_TRIANGLES;

    public static final int
        SHADE_DEFAULT = 0,
        SHADE_OUTLINE = 1;

    static int count = 0;
    private int vaoId;

    private List<Integer>vboIdList = null;
    private int vertexCount;

    private int primitiveType;
    public int shadeType;

    private Material material;

    boolean hasPositions = false;
    float[] positions;
    float[] colors;
    private BoundingBox bbox;

    public Mesh(int primitiveType, int shadeType, float[] positions, float[] colors, float[] textCoords, float[] normals, int[] indices) {

        this(primitiveType, shadeType, positions, colors, textCoords, normals, indices, new BoundingBox());
    }

    public Mesh(int primitiveType, int shadeType, float[] positions, float[] colors, float[] textCoords, float[] normals, int[] indices, BoundingBox bbox) {
        set(primitiveType, shadeType, positions, colors, textCoords, normals, indices, bbox);
    }

  
    public void set(int primitiveType, int shadeType, float[] positions, float[] colors, float[] textCoords, float[] normals, int[] indices) {
        
        // TODO: Ewgross
        if (bbox == null) {
            if (this.bbox == null) {
                bbox = new BoundingBox();
            }
        }

        set(primitiveType, shadeType, positions, colors, textCoords, normals, indices, bbox);
    }

    public void set(int primitiveType, int shadeType, float[] positions, float[] colors, float[] textCoords, float[] normals, int[] indices, BoundingBox bbox) {
        
        // TODO: bbox is not being auto-sized.

        this.shadeType = shadeType;

        // If shadeType is SHADE_OUTLINE, generate smooth normals and stash them in the color channel.
        if (shadeType == Mesh.SHADE_OUTLINE) {
            generateOutlineNormals(positions, colors, normals);
        }

        FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer colBuffer = null;

        boolean newMesh = vboIdList == null;

        try {
            this.primitiveType = primitiveType;
            
            vertexCount = indices.length;

            if (newMesh) {
                vboIdList = new ArrayList<>();

                vaoId = glGenVertexArrays();
            }
        
            glBindVertexArray(vaoId);

            // Position VBO
            this.positions = positions;
            this.hasPositions = true;

            int vboId;
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(0);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);



            // Color VBO
            this.colors = colors;

            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);                
            } else {
                vboId = vboIdList.get(1);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            colBuffer = MemoryUtil.memAllocFloat(colors.length);
            colBuffer.put(colors).flip();
            glBufferData(GL_ARRAY_BUFFER, colBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

            // Texture Coordinates VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(2);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);

            // Normal VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(3);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

            // Index VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(4);
            }

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);

            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            if (newMesh) {
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
            
                this.bbox = bbox;
            }
            
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }

            if (colBuffer != null) {
                MemoryUtil.memFree(colBuffer);
            }

            if (textCoordsBuffer != null) {
                MemoryUtil.memFree(textCoordsBuffer);
            }

            if (vecNormalsBuffer != null) {
                MemoryUtil.memFree(vecNormalsBuffer);
            }

            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public void generateOutlineNormals(float[] positions, float[] colors, float[] normals) {

        int positionCount = 0;
        Vector3f smoothNormal = new Vector3f();

        for (int i = 0; i < positions.length; i += 3) {

            positionCount = 0;
            smoothNormal.zero();
            
            for (int j = 0; j < positions.length; j += 3) {

                if (positions[i] == positions[j] && positions[i+1] == positions[j+1] && positions[i+2] == positions[j+2]) {

                    positionCount++;
                    smoothNormal.x += normals[j];
                    smoothNormal.y += normals[j+1];
                    smoothNormal.z += normals[j+2];
                }
            }

            if (positionCount > 0) {

                smoothNormal.x /= (float)positionCount;
                smoothNormal.y /= (float)positionCount;
                smoothNormal.z /= (float)positionCount;

                smoothNormal.normalize();

                colors[i]   = smoothNormal.x;
                colors[i+1] = smoothNormal.y;
                colors[i+2] = smoothNormal.z;
            }
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getVaoId() {

        return vaoId;
    }

    public int getVertexCount() {

        return vertexCount;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public void beginRender() {

        Texture texture = material != null ? material.getTexture() : null;
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0);

            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }

        // Bind to the VAO
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
    }

    public void endRender() {
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render() {

        beginRender();

        // Draw the vertices
        glDrawElements(primitiveType, getVertexCount(), GL_UNSIGNED_INT, 0);
        endRender();
    }

    public int renderList(int curLayer, List<Entity> entities, Consumer<Entity> consumer) {
    
        int numRemainingLayers = 0;

        beginRender();

        for (Entity entity : entities) {
    
            if (entity.getVisible() && entity.getParentVisible()) {
    
                int layer = entity.getLayer();
                if (layer > curLayer) {
                    numRemainingLayers++;
                } else if (layer == curLayer) {

                    consumer.accept(entity);
                    glDrawElements(primitiveType, getVertexCount(), GL_UNSIGNED_INT, 0);
                }
            }   
        }

        endRender();

        return numRemainingLayers;
    }

    public void deleteBuffers() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        vboIdList = null;

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void shutdown() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        Texture texture = material.getTexture();
        if (texture != null) {
            texture.shutdown();
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}