package org.engine.ui;

import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.Utilities;
import org.engine.core.Rect;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Entity;
import org.engine.renderer.Window;

public class UiElement extends Entity {

    protected RectTransform rectTrans;
    protected Material material;

    protected Canvas canvas = null;

    public UiElement() {
        super();

        rectTrans = new RectTransform();
        material = new Material();
    }

    public UiElement(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot) {

        super();

        this.canvas = canvas;
        setParent(parent);

        rectTrans = new RectTransform();

        rectTrans.pivot.set(pivot);

        rectTrans.rect.set(rect);

        rectTrans.anchor.set(anchor);

        material = new Material();

        if (parent != null) {
            float parentDepth = rectTrans.getDepth();
            rectTrans.setDepth(parentDepth + 0.01f);
        }

        updateSize();
    }

    public void setAnchor(Rect anchor) {
        rectTrans.anchor = anchor;
    }

    public void updateSize() {

        update();
    }

    public void update() {

        Rect oldScreenRect = rectTrans.screenRect.copy();

        if (parent == null) {
            // This is the canvas.
            rectTrans.globalRect.set(rectTrans.rect);
        } else {
            // This is a child or ancestor of the canvas.

            UiElement parentElement = (UiElement)parent;
            Rect parentRect = parentElement.rectTrans.getGlobalRect();

            // Calculate screen rect relative to parent.

            Vector2f pivot = new Vector2f(rectTrans.pivot);
            Rect anchor = rectTrans.anchor.copy();

            pivot.x = rectTrans.rect.getWidth() * pivot.x;
            pivot.y = rectTrans.rect.getHeight() * pivot.y;

            anchor.xMin = parentRect.xMin + anchor.xMin * parentRect.xMax;
            anchor.yMin = parentRect.yMin + anchor.yMin * parentRect.yMax;
            anchor.xMax = parentRect.xMin + anchor.xMax * parentRect.xMax;
            anchor.yMax = parentRect.yMin + anchor.yMax * parentRect.yMax;

            rectTrans.globalRect.xMin = anchor.xMin + rectTrans.rect.xMin - pivot.x;
            rectTrans.globalRect.yMin = anchor.yMin + rectTrans.rect.yMin - pivot.y;
            rectTrans.globalRect.xMax = rectTrans.globalRect.xMin + rectTrans.rect.getWidth();
            rectTrans.globalRect.yMax = rectTrans.globalRect.yMin + rectTrans.rect.getHeight();
        }

        // To screen space.
        rectTrans.screenRect.set(rectTrans.globalRect);

        // Use the canvas working resolution and reference resolution to calculate the screen space scale factor.
        if (canvas != null) {
            rectTrans.screenRect.scale(canvas.getReferenceScale());
        }


        // If the screen rect didn't change, the children don't need to be updated.
        if (rectTrans.screenRect.equals(oldScreenRect)) {
            return;
        }

        buildMesh();

        if (children != null) {
            for (Entity childEntity : children) {

                UiElement childElement = (UiElement)childEntity;
                childElement.update();
            }
        }
    }

    private void buildMesh() {

        Mesh mesh = getMesh();
        if (mesh != null) {
            mesh.deleteBuffers();
        }

        float depth = rectTrans.getDepth();

        float[] positions = new float[4 * 3];
        float[] texCoords = new float[4 * 2];
        int[] indices = new int[6];

        // Top left
        positions[0] = rectTrans.screenRect.xMin;
        positions[1] = rectTrans.screenRect.yMin;
        positions[2] = depth;

        texCoords[0] = 0.0f;
        texCoords[1] = 0.0f;

        // Top right
        positions[3] = rectTrans.screenRect.xMax;
        positions[4] = rectTrans.screenRect.yMin;
        positions[5] = depth;

        texCoords[2] = 1.0f;
        texCoords[3] = 0.0f;

        // Bottom right
        positions[6] = rectTrans.screenRect.xMax;
        positions[7] = rectTrans.screenRect.yMax;
        positions[8] = depth;

        texCoords[4] = 1.0f;
        texCoords[5] = 1.0f;

        // Bottom left
        positions[9] = rectTrans.screenRect.xMin;
        positions[10] = rectTrans.screenRect.yMax;
        positions[11] = depth;

        texCoords[6] = 0.0f;
        texCoords[7] = 1.0f;

        indices[0] = 1;
        indices[1] = 0;
        indices[2] = 3;

        indices[3] = 3;
        indices[4] = 2;
        indices[5] = 1;

        float[] normals = new float[0];

        mesh = new Mesh(positions, texCoords, normals, indices);
        mesh.setMaterial(material);

        setMesh(mesh);
    }
}
