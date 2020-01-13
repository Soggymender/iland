package org.engine.ui;

import org.joml.Vector2f;

import org.engine.core.Rect;

public class RectTransform {

    protected Vector2f pivot;

    protected Rect rect;            // Local rect, relative to parent.
    protected Rect anchor;

    private float depth = 0.0f;

    protected Rect globalRect;      // rect transformed by parent.
    protected Rect screenRect;      // rect transformed into screen space;

    public RectTransform() {
        pivot = new Vector2f();

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
