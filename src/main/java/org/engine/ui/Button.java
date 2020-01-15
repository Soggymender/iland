package org.engine.ui;

import org.engine.input.Mouse;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import org.engine.renderer.FontTexture;
import org.engine.scene.Entity;
import org.engine.core.Rect;

public class Button extends UiElement {

    private static final float HIGHLIGHTED_COLOR_OFFSET = 0.05f;
    private static final float PRESSED_COLOR_OFFSET = 0.15f;
    private static final float PRESSED_TIME = 0.125f;

    private Vector4f color;
    private Vector4f highlightedColor;
    private Vector4f pressedColor;

    private boolean  highlighted = false;
    private boolean  pressed = false;

    private float pressedTime = 0.0f;

    private Text text = null;
    private FontTexture fontTexture = null;


    public Button(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot, String textString, FontTexture fontTexture) throws Exception {

        super(canvas, parent, rect, anchor, pivot);

        java.util.Random rand = new java.util.Random();

        color = new Vector4f(rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, rand.nextFloat() % 1.0f, 1.0f);
        highlightedColor = new Vector4f(color.x + HIGHLIGHTED_COLOR_OFFSET, color.y + HIGHLIGHTED_COLOR_OFFSET, color.z + HIGHLIGHTED_COLOR_OFFSET, color.w);
        pressedColor = new Vector4f(color.x + PRESSED_COLOR_OFFSET, color.y + PRESSED_COLOR_OFFSET, color.z + PRESSED_COLOR_OFFSET, color.w);

        forwardsInput = false;
        acceptsInput = true;
        buildsMesh = true;

        material.setDiffuseColor(color);

        update();

        if (textString != null) {
            this.fontTexture = fontTexture;

            text = new Text(canvas, this, new Rect(0, 0, 1, 1, true), new Rect(0, 0, 1, 1, true), pivot, textString, fontTexture);
        }
    }

    public void setText(String textString) throws Exception{

        if (text == null) {
            return;
        }

        if (textString == null || textString.length() == 0) {
            clearText();
            return;
        }

        text.setText(textString);
    }

    public void clearText() {

        if (text == null) {
            return;
        }

        text.getMesh().deleteBuffers();
        text = null;
    }

    public Text getText() {
        return text;
    }

    public boolean input(Mouse mouse, float interval) {

        Vector2f pos = mouse.getPosition();

        // End pressed state?
        if (pressed) {
            if (pressedTime >= PRESSED_TIME) {
                pressedTime = 0.0f;
                pressed = false;
                material.setDiffuseColor(color);
            }

            pressedTime += interval;
        } else {
            if (rectTrans.pointInRect(pos)) {

                if (mouse.leftButtonJustPressed()) {
                    pressed = true;
                    pressedTime = 0.0f;

                    material.setDiffuseColor(pressedColor);
                } else {
                    highlighted = true;
                    material.setDiffuseColor(highlightedColor);
                }
            } else {

                if (highlighted) {
                    material.setDiffuseColor(color);
                }
            }
        }

        return false;
    }
}