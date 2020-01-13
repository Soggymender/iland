package org.engine.ui;

import java.util.ArrayList;
import java.util.List;

import org.engine.core.Rect;
import org.engine.renderer.Mesh;
import org.joml.Vector4f;

public class RectTransform {

//    public float xPivot;
//    public float yPivot;

    protected Rect rect;            // Local rect, relative to parent.
    protected Rect anchor;

    private float depth = 0.0f;

    protected Rect globalRect;      // rect transformed by parent.
    protected Rect screenRect;      // rect transformed into screen space;

    public RectTransform() {
        rect = new Rect();
        anchor = new Rect();

        globalRect = new Rect();
        screenRect = new Rect();
    }

    public Rect getRect() {
        return rect.copy();
    }

    public Rect getGlobalRect() {
        return globalRect.copy();
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }
}
