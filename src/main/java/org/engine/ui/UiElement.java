package org.engine.ui;

import org.engine.renderer.ShaderCache;
import org.joml.Vector2f;

import org.engine.core.Rect;
import org.engine.input.*;
import org.engine.renderer.Color;
import org.engine.renderer.Material;
import org.engine.renderer.Shader;
import org.engine.renderer.Mesh;
import org.engine.scene.Entity;

class MeshData {

    public int vertexCount = 0;
    public float[] positions;
    public int positionsCount = 0;
    public float[] colors;
    public int colorsCount = 0;
    public float[] texCoords;
    public int texCoordsCount = 0;
    public int[] indices;
    public int indexCount = 0;
}

public class UiElement extends Entity {

    protected class Flags {
        protected boolean forwardsInput = false;
        protected boolean acceptsInput = false;
        protected boolean buildsMesh = false;
        protected boolean dirty = true; // Everything starts dirty so it'll do an initial update after construction.
        protected boolean rebuild = false;

        protected boolean hasTail = false;
    }

    protected float cornerRadius = 0;
    protected RectTransform rectTrans;

    protected float tailWidth = 20.0f;
    protected float tailHeight = 20.0f;
    protected Vector2f tailTarget = null;

    protected Material material;

    protected Canvas canvas = null;

    protected Flags flags = new Flags();

    public UiElement() throws Exception {
        super();

        rectTrans = new RectTransform();
        material = new Material();

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultGuiShader = shaderCache.getShader("defaultGui");

        material.setShader(defaultGuiShader);
    }

    public UiElement(Canvas canvas, Entity parent, Rect rect, float cornerRadius, Rect anchor, Vector2f pivot) throws Exception {

        super();

        this.canvas = canvas;
        setParent(parent);

        rectTrans = new RectTransform();

        rectTrans.pivot.set(pivot);

        rectTrans.rect.set(rect);

        this.cornerRadius = cornerRadius;

        rectTrans.anchor.set(anchor);

        material = new Material();

        ShaderCache shaderCache = ShaderCache.getInstance();
        Shader defaultGuiShader = shaderCache.getShader("defaultGui");

        material.setShader(defaultGuiShader);

        // Automatically draw in front of the parent.
        if (parent != null) {
            UiElement parentElem = (UiElement)parent;
            float parentDepth = parentElem.rectTrans.getDepth();
            rectTrans.setDepth(parentDepth + 0.01f);
        }
    }

    public void setAnchor(Rect anchor) {
        rectTrans.anchor = anchor;
    }

    public void setTailSize(float width, float height) {
        tailWidth = width;
        tailHeight = height;
    }

    public void setTailTarget(Vector2f target) {
        flags.hasTail = true;
        tailTarget = target;

       // flags.rebuild = true;
        buildMesh();
    }

    public float getDepth() {
        return rectTrans.getDepth();
    }

    public void setDepth(float depth) {
        rectTrans.setDepth(depth);
    }

    public void setColor(Color color) {
        material.setDiffuseColor(color);
    }

    public Rect getScreenRect() {
        return rectTrans.screenRect.copy();
    }

    public void updateSize() {
        flags.dirty = true;

        // Recursively update the size of everything in the hierarchy to flag an update.
        if (children == null) {
            return;
        }

        for (Entity child : children) {
            if (child instanceof UiElement) {
                ((UiElement)child).updateSize();
            }
        }
    }

    @Override
    public void input(Input input) {

        if (!flags.forwardsInput) {
            return;
        }

        // Leafs are most user-facing so walk all the way down and work back up.

        super.input(input);
    }

    @Override
    public void update(float interval) {

        if (!flags.dirty && !flags.rebuild) {
            super.update(interval);
            return;
        }

        super.update(interval);

        flags.dirty = false;

        Rect oldScreenRect = rectTrans.screenRect.copy();

        // Use the canvas working resolution and reference resolution to calculate the screen space scale factor.
        float scaleFactor = 1.0f;

        if (canvas != null) {
            scaleFactor = canvas.getReferenceScale();
        }

        if (this == canvas) {
            // This is the canvas.

            rectTrans.globalRect.set(rectTrans.rect);
            //rectTrans.globalRect.scale(scaleFactor);
        } else {
            // This is a child or ancestor of the canvas.

            UiElement parentElement = (UiElement)parent;
            Rect parentRect = parentElement.rectTrans.getGlobalRect();

            // Calculate screen rect relative to parent.

            Vector2f pivot = new Vector2f(rectTrans.pivot);
            Rect anchor = rectTrans.anchor.copy();

            pivot.x = rectTrans.rect.getWidth() * scaleFactor * pivot.x;
            pivot.y = rectTrans.rect.getHeight() * scaleFactor * pivot.y;

            anchor.xMin = parentRect.xMin + anchor.xMin * parentRect.xMax;
            anchor.yMin = parentRect.yMin + anchor.yMin * parentRect.yMax;
            anchor.xMax = parentRect.xMin + anchor.xMax * (parentRect.xMax - parentRect.xMin);
            anchor.yMax = parentRect.yMin + anchor.yMax * (parentRect.yMax - parentRect.yMin);

            // If x axis anchors are equal, xMax represents width, otherwise it represents an offset from anchor xMax.
            // Similarly for y axis and height.
            // This model is based on observations from the Unity UI system.
            boolean useWidth  = anchor.xMin == anchor.xMax;
            boolean useHeight = anchor.yMin == anchor.yMax;

            if (useWidth) {
                rectTrans.globalRect.xMin = anchor.xMin + rectTrans.rect.xMin * scaleFactor - pivot.x;
                rectTrans.globalRect.xMax = rectTrans.globalRect.xMin + rectTrans.rect.getWidth() * scaleFactor;
            } else {
                rectTrans.globalRect.xMin = anchor.xMin + rectTrans.rect.xMin * scaleFactor - pivot.x;
                rectTrans.globalRect.xMax = anchor.xMax + rectTrans.rect.xMax * scaleFactor - pivot.x;
            }

            if (useHeight) {
                rectTrans.globalRect.yMin = anchor.yMin + rectTrans.rect.yMin * scaleFactor - pivot.y;
                rectTrans.globalRect.yMax = rectTrans.globalRect.yMin + rectTrans.rect.getHeight() * scaleFactor;
            } else {
                rectTrans.globalRect.yMin = anchor.yMin + rectTrans.rect.yMin * scaleFactor - pivot.y;
                rectTrans.globalRect.yMax = anchor.yMax + rectTrans.rect.yMax * scaleFactor - pivot.y;

            }
        }

        // To screen space.
        rectTrans.screenRect.set(rectTrans.globalRect);
        float tempMax = rectTrans.screenRect.yMax;
        rectTrans.screenRect.yMax = canvas.workingResolution.y - rectTrans.screenRect.yMin;
        rectTrans.screenRect.yMin = canvas.workingResolution.y - tempMax;
        

        // If the screen rect didn't change, the children don't need to be updated.
        if (rectTrans.screenRect.equals(oldScreenRect) && !flags.rebuild) {
            return;
        }

        if (flags.buildsMesh) {
            flags.rebuild = false;

            buildMesh();
        }
    }

    private void buildMesh() {

        if (cornerRadius > 0) {
            buildRoundedMesh();
            return;
        }

        Mesh mesh = getMesh();
        if (mesh != null) {
            mesh.deleteBuffers();
        }

        MeshData meshData = new MeshData();

        meshData.positions = new float[4 * 3];
        meshData.colors = new float[4 * 4];
        meshData.texCoords = new float[4 * 2];
        meshData.indices = new int[6];

        Rect rect = new Rect(rectTrans.screenRect.xMin, rectTrans.screenRect.yMin, rectTrans.screenRect.xMax, rectTrans.screenRect.yMax, true);

        addMeshRect(rect, meshData);

        float[] normals = new float[0];

        mesh = new Mesh(Mesh.TRIANGLES, Mesh.SHADE_DEFAULT, meshData.positions, meshData.colors, meshData.texCoords, normals, meshData.indices);
        mesh.setMaterial(material);

        setMesh(mesh);
    }

    private void buildRoundedMesh() {

        Mesh mesh = getMesh();
        if (mesh != null) {
            mesh.deleteBuffers();
        }

        MeshData meshData = new MeshData();

        int numSegments = 3;

        int numPositions = (1 + numSegments + 1) * 3 * 4;
        int numColors = (1 + numSegments + 1) * 4 * 4;
        int numTexCoords = (1 + numSegments + 1) * 2 * 4;
        int numIndices = (numSegments + 1) * 3 * 4;

        if (flags.hasTail) {
            numPositions += 9;
            numColors += 12;
            numTexCoords += 6;
            numIndices += 3;
        }

        meshData.positions = new float[4 * 3 * 3 + numPositions];
        meshData.colors = new float[4 * 4 * 3 + numColors];
        meshData.texCoords = new float[4 * 2 * 3 + numTexCoords];
        meshData.indices = new int[6 * 3 + numIndices];

        Rect rect;
        
        // Bottom slice.
        rect = new Rect(rectTrans.screenRect.xMin + cornerRadius, rectTrans.screenRect.yMin, rectTrans.screenRect.xMax - cornerRadius, rectTrans.screenRect.yMin + cornerRadius, true);
        addMeshRect(rect, meshData);

        // Middle slice.
        rect = new Rect(rectTrans.screenRect.xMin, rectTrans.screenRect.yMin + cornerRadius, rectTrans.screenRect.xMax, rectTrans.screenRect.yMax - cornerRadius, true);
        addMeshRect(rect, meshData);

        // Top slice.
        rect = new Rect(rectTrans.screenRect.xMin + cornerRadius, rectTrans.screenRect.yMax - cornerRadius, rectTrans.screenRect.xMax - cornerRadius, rectTrans.screenRect.yMax, true);
        addMeshRect(rect, meshData);

                // Add tail.
                if (flags.hasTail) {
                    addMeshTail(rectTrans.screenRect.xMin + cornerRadius, rectTrans.screenRect.xMax - cornerRadius, rectTrans.screenRect.yMin, tailWidth, tailHeight, tailTarget, meshData);
                }
        
        // Add corners.
        addMeshCorner(rectTrans.screenRect.xMin + cornerRadius, rectTrans.screenRect.yMax - cornerRadius, cornerRadius, -180.0f, -90.0f, numSegments, meshData);
        addMeshCorner(rectTrans.screenRect.xMax - cornerRadius, rectTrans.screenRect.yMax - cornerRadius, cornerRadius, -90.0f, 0.0f, numSegments, meshData);
        addMeshCorner(rectTrans.screenRect.xMin + cornerRadius, rectTrans.screenRect.yMin + cornerRadius, cornerRadius,  90.0f, 180.0f, numSegments, meshData);
        addMeshCorner(rectTrans.screenRect.xMax - cornerRadius, rectTrans.screenRect.yMin + cornerRadius, cornerRadius,   0.0f, 90.0f, numSegments, meshData);


        float[] normals = new float[0];

        if (mesh == null) 
            mesh = new Mesh(Mesh.TRIANGLES, Mesh.SHADE_DEFAULT, meshData.positions, meshData.colors, meshData.texCoords, normals, meshData.indices);
        else
            mesh.set(Mesh.TRIANGLES, Mesh.SHADE_DEFAULT, meshData.positions, meshData.colors, meshData.texCoords, normals, meshData.indices);

        mesh.setMaterial(material);

        setMesh(mesh);
    }

    private void addMeshRect(Rect rect, MeshData meshData) {

        float depth = rectTrans.getDepth();

        float halfWidth = canvas.workingResolution.x / 2.0f;
        float halfHeight = canvas.workingResolution.y / 2.0f;

        int vc = meshData.vertexCount;
        int pc = meshData.positionsCount;
        int tc = meshData.texCoordsCount;
        int ic = meshData.indexCount;

        // Top left
        meshData.positions[pc + 0] = rect.xMin - halfWidth;
        meshData.positions[pc + 1] = rect.yMax - halfHeight;
        meshData.positions[pc + 2] = depth;

        meshData.texCoords[tc + 0] = 0.0f;
        meshData.texCoords[tc + 1] = 0.0f;

        // Top right
        meshData.positions[pc + 3] = rect.xMax - halfWidth;
        meshData.positions[pc + 4] = rect.yMax - halfHeight;
        meshData.positions[pc + 5] = depth;

        meshData.texCoords[tc + 2] = 1.0f;
        meshData.texCoords[tc + 3] = 0.0f;

        // Bottom right
        meshData.positions[pc + 6] = rect.xMax - halfWidth;
        meshData.positions[pc + 7] = rect.yMin - halfHeight;
        meshData.positions[pc + 8] = depth;

        meshData.texCoords[tc + 4] = 1.0f;
        meshData.texCoords[tc + 5] = 1.0f;

        // Bottom left
        meshData.positions[pc + 9] = rect.xMin - halfWidth;
        meshData.positions[pc + 10] = rect.yMin - halfHeight;
        meshData.positions[pc + 11] = depth;

        meshData.texCoords[tc + 6] = 0.0f;
        meshData.texCoords[tc + 7] = 1.0f;

        for (int i = 0; i < 4; i++) {

            int cc = meshData.colorsCount;

            meshData.colors[cc + 0] = 1.0f;
            meshData.colors[cc + 1] = 1.0f;
            meshData.colors[cc + 2] = 1.0f;
            meshData.colors[cc + 3] = 1.0f;

            meshData.colorsCount += 4;
        }

        meshData.indices[ic + 0] = vc + 1;
        meshData.indices[ic + 1] = vc + 0;
        meshData.indices[ic + 2] = vc + 3;

        meshData.indices[ic + 3] = vc + 3;
        meshData.indices[ic + 4] = vc + 2;
        meshData.indices[ic + 5] = vc + 1;
 
        meshData.vertexCount += 4;
        meshData.positionsCount += 12;
        meshData.texCoordsCount += 8;
        meshData.indexCount += 6;
    }

    private void addMeshCorner(float x, float y, float radius, float startAngle, float endAngle, int numSegments, MeshData meshData) {

        float depth = rectTrans.getDepth();

        float halfWidth = canvas.workingResolution.x / 2.0f;
        float halfHeight = canvas.workingResolution.y / 2.0f;

        int vc = meshData.vertexCount;
        int pc = meshData.positionsCount;
        int cc = meshData.colorsCount;
        int tc = meshData.texCoordsCount;
        int ic = meshData.indexCount;

        // Add the pivot.
        int pivotIdx = vc;

        meshData.positions[pc + 0] = x - halfWidth;
        meshData.positions[pc + 1] = y - halfHeight;
        meshData.positions[pc + 2] = depth;

        meshData.colors[cc + 0] = 1.0f;
        meshData.colors[cc + 1] = 1.0f;
        meshData.colors[cc + 2] = 1.0f;
        meshData.colors[cc + 3] = 1.0f;

        meshData.texCoords[tc + 0] = 0.0f;
        meshData.texCoords[tc + 1] = 0.0f;

        vc++;
        pc += 3;
        cc += 4;
        tc += 2;
    

        float angle = startAngle;
        float segmentAngle = (endAngle - startAngle) / numSegments;

        for (int i = 0; i < numSegments + 1; i++) {

            float rads = (float)Math.toRadians(angle);
            float posX = cornerRadius * (float)Math.cos(rads);
            float posY = cornerRadius * (float)Math.sin(rads);

            meshData.positions[pc + 0] = x + posX - halfWidth;
            meshData.positions[pc + 1] = y - posY - halfHeight;
            meshData.positions[pc + 2] = depth;

            meshData.colors[cc + 0] = 1.0f;
            meshData.colors[cc + 1] = 1.0f;
            meshData.colors[cc + 2] = 1.0f;
            meshData.colors[cc + 3] = 1.0f;

            meshData.texCoords[tc + 0] = 0.0f;
            meshData.texCoords[tc + 1] = 0.0f;

            // Add a triangle as long as this isn't the first position.
            if (i > 0) {

                meshData.indices[ic + 0] = pivotIdx;
                meshData.indices[ic + 1] = vc - 1;
                meshData.indices[ic + 2] = vc;        
            }

            vc++;
            pc += 3;
            cc += 4;
            tc += 2;
            ic += 3;

            angle += segmentAngle;
        }

        meshData.vertexCount = vc;
        meshData.positionsCount = pc;
        meshData.colorsCount = cc;
        meshData.texCoordsCount = tc;
        meshData.indexCount = ic;
    }

    private void addMeshTail(float xMin, float xMax, float yMin, float tailWidth, float tailHeight, Vector2f tailTarget, MeshData meshData) {

        float tailOffset = tailWidth * 0.65f;

        float depth = rectTrans.getDepth();

        float halfWidth = canvas.workingResolution.x / 2.0f;
        float halfHeight = canvas.workingResolution.y / 2.0f;

        int vc = meshData.vertexCount;
        int pc = meshData.positionsCount;
        int cc = meshData.colorsCount;
        int tc = meshData.texCoordsCount;
        int ic = meshData.indexCount;

        // Top left
        meshData.positions[pc + 0] = xMax - tailWidth - tailOffset - halfWidth;
        meshData.positions[pc + 1] = yMin - halfHeight;
        meshData.positions[pc + 2] = depth;

        meshData.texCoords[tc + 0] = 0.0f;
        meshData.texCoords[tc + 1] = 0.0f;

        // Top right
        meshData.positions[pc + 3] = xMax - tailOffset - halfWidth;
        meshData.positions[pc + 4] = yMin - halfHeight;
        meshData.positions[pc + 5] = depth;

        meshData.texCoords[tc + 2] = 1.0f;
        meshData.texCoords[tc + 3] = 0.0f;

        // Bottom right
//        meshData.positions[pc + 6] = xMax - halfWidth;
//        meshData.positions[pc + 7] = yMin - tailHeight - halfHeight;

        float tailStartX = meshData.positions[pc + 0] + halfWidth + ((meshData.positions[pc + 3] - meshData.positions[pc + 0]) / 2.0f);
        float tailStartY = yMin;// - halfHeight; 

        Vector2f tailDir = new Vector2f(tailTarget.x, tailTarget.y + 1.65f);

        tailDir.sub(new Vector2f(tailStartX, tailStartY - tailHeight));        


        float tailY = tailTarget.y + 0.65f;
        //if ((yMin - halfHeight) - tailY < tailHeight)
        //    tailY = tailHeight;

        /*
        Create vector to target
        Find intersect point at tailHeight
        
        x = y/m - b/m
        
        y = mx + b
        b = y - mx
        */

        float slope = tailDir.y / tailDir.x;

        float yIntercept = tailStartY - (slope * tailStartX);

        float x = ((tailStartY - tailHeight)/ slope) - (yIntercept / slope);



        meshData.positions[pc + 6] = x - halfWidth;
        meshData.positions[pc + 7] = yMin - halfHeight - tailHeight;// - halfHeight;

        meshData.positions[pc + 8] = depth;

        meshData.texCoords[tc + 4] = 1.0f;
        meshData.texCoords[tc + 5] = 1.0f;

        meshData.indices[ic + 0] = vc + 0;
        meshData.indices[ic + 1] = vc + 1;
        meshData.indices[ic + 2] = vc + 2;

        for (int i = 0; i < 3; i++) {

            meshData.colors[cc + 0] = 1.0f;
            meshData.colors[cc + 1] = 1.0f;
            meshData.colors[cc + 2] = 1.0f;
            meshData.colors[cc + 3] = 1.0f;

            cc += 4;
        }

        meshData.vertexCount += 3;
        meshData.positionsCount += 9;
        meshData.colorsCount += 12;
        meshData.texCoordsCount += 6;
        meshData.indexCount += 3;
    }
}