package org.engine.ui;

import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.scene.Entity;
import org.engine.core.Rect;

public class Panel extends UiElement {

    public Panel(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot) {

        super(canvas, parent, rect, anchor, pivot);

        material.setDiffuseColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
    }
}
