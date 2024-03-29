package org.engine.ui;

import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.scene.Entity;
import org.engine.core.Rect;

public class Panel extends UiElement {

    public Panel(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot) throws Exception {

        super(canvas, parent, rect, anchor, pivot);

        java.util.Random rand = new java.util.Random();

        flags.forwardsInput = true;
        flags.acceptsInput = false;
        flags.buildsMesh = true;

        material.setDiffuseColor(new Vector4f(rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, 1.0f));
    }
}
