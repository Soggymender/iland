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

public class Mesh {

    public static final int
        LINES          = GL_LINES,
        TRIANGLES      = GL_TRIANGLES;

    static int count = 0;
    private int vaoId;

    private List<Integer>vboIdList = null;
    private int vertexCount;

    private int primitiveType;

    private Material material;

    boolean hasPositions = false;
    float[] positions;
    private BoundingBox bbox;

    public Mesh(int primitiveType, float[] positions, float[] textCoords, float[] normals, int[] indices) {

        this(primitiveType, positions, textCoords, normals, indices, new BoundingBox());
    }

    public Mesh(int primitiveType, float[] positions, float[] textCoords, float[] normals, int[] indices, BoundingBox bbox) {
        set(primitiveType, positions, textCoords, normals, indices, bbox);
    }

  
    public void set(int primitiveType, float[] positions, float[] textCoords, float[] normals, int[] indices) {
        
        

        // TODO: Ewgross
        if (bbox == null) {
            if (this.bbox == null) {
                bbox = new BoundingBox();
            }
        }

        set(primitiveType, positions, textCoords, normals, indices, bbox);
    }

    public void set(int primitiveType, float[] positions, float[] textCoords, float[] normals, int[] indices, BoundingBox bbox) {
        
        // TODO: bbox is not being auto-sized.

        FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        IntBuffer indicesBuffer = null;

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

            // Texture Coordinates VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(1);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Normal VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(2);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Index VBO
            if (newMesh) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                
            } else {
                vboId = vboIdList.get(3);
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
    }

    public void endRender() {
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
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