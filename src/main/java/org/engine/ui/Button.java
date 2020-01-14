package org.engine.ui;

import org.engine.input.Mouse;
import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.scene.Entity;
import org.engine.core.Rect;

public class Button extends UiElement {

    private static final float HIGHLIGHTED_COLOR_OFFSET = 0.1f;

    private Vector4f color;
    private Vector4f highlightedColor;
    private boolean  highlighted = false;

    public Button(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot) {

        super(canvas, parent, rect, anchor, pivot);

        java.util.Random rand = new java.util.Random();

        color = new Vector4f(rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, 1.0f);

        highlightedColor = new Vector4f(color.x + HIGHLIGHTED_COLOR_OFFSET, color.y + HIGHLIGHTED_COLOR_OFFSET, color.z + HIGHLIGHTED_COLOR_OFFSET, color.w);

        material.setDiffuseColor(color);
    }

    public boolean input(Mouse mouse) {

        Vector2f pos = mouse.getPosition();

        if (rectTrans.pointInRect(pos)) {

            highlighted = true;
            material.setDiffuseColor(highlightedColor);
        } else {

            if (highlighted) {
                material.setDiffuseColor(color);
            }
        }

        return false;
    }
}