package org.engine.ui;

import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.input.*;
import org.engine.renderer.Window;

public class Canvas extends UiElement {

    private Vector2f referenceResolution;
    private Vector2f workingResolution;

    private boolean useWidth = true;

    public Canvas(Window window, Vector2f referenceResolution) {

        super();
        canvas = this;

        this.referenceResolution = referenceResolution;
        workingResolution = new Vector2f();

        material.setDiffuseColor(new Vector4f(1.0f, 0.0f, 0.0f, 0.5f));

        flags.forwardsInput = true;
        flags.acceptsInput = false;
        flags.buildsMesh = false;

        updateSize(window);
    }

    public void updateSize(Window window) {

        // Keep the canvas at the reference resolution. Changing the window size only affects the reference scale.
        Vector2f oldWorkingResolution = new Vector2f(workingResolution);

        workingResolution.x = window.getWidth();
        workingResolution.y = window.getHeight();

        rectTrans.rect.set(0, 0, workingResolution.x, workingResolution.y);

        if (workingResolution.x != oldWorkingResolution.x || workingResolution.y != oldWorkingResolution.y) {
            super.updateSize();
        }
    }

    @Override
    public void input(Input input) {

        // Make sure the mouse is active.
        super.input(input);
        return;

        // Walk to the children and work up.
        // Leafs are drawn and detected top-most.
    }

    public float getReferenceScale() {

        if (useWidth) {
            return workingResolution.x / referenceResolution.x;
        } else {
            return workingResolution.y / referenceResolution.y;
        }
    }
}
