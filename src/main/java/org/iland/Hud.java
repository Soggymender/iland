package org.iland;

import java.awt.Font;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Rect;
import org.joml.Vector2f;

import org.engine.input.*;
import org.engine.renderer.FontTexture;
import org.engine.scene.Scene;
import org.engine.ui.Button;
import org.engine.ui.Canvas;
import org.engine.ui.Panel;
import org.engine.ui.Text;

import org.engine.renderer.Window;

public class Hud {

    private Canvas canvas;
    private Panel bPanel;

    private static final Font FONT = new Font("Arial", Font.PLAIN, 24);
    private static final String CHARSET = "ISO-8859-1";

    private final Text fpsText;

    public Hud(Window window, Scene scene) throws Exception {

        FontTexture fontTexture = new FontTexture(FONT, CHARSET);

        canvas = new Canvas(window, new Vector2f(720, 480));
        bPanel = new Panel(canvas,  canvas, new Rect(0, 0, -100, -100, true), new Rect(0, 0, 1, 1),       new Vector2f(0, 0));
        new Button(canvas, bPanel, new Rect(100, 100, 100, 30),      new Rect(0, 0, 0, 0),       new Vector2f(0, 0), "test words in a tiny button", fontTexture);
        new Panel(canvas,  bPanel, new Rect(-100, 100, 100, 50),     new Rect(1, 0, 1, 0, true), new Vector2f(1, 0));

        // Setup a text box.
        fpsText = new Text(canvas, canvas, new Rect(5, -5, 100, 20), new Rect(0, 1, 0, 1, true), new Vector2f(0, 1), "0.0", fontTexture);;

        bPanel.setVisible(false);

        scene.addEntity(canvas);
    }

    public void setStatusText(String statusText) {
        fpsText.setText(statusText);
    }

    public void input(Input input) {

        Keyboard keyboard = input.getKeyboard();

        if (keyboard.keyJustDown(GLFW_KEY_TAB)) {
            
            boolean hidden = bPanel.getVisible();
            bPanel.setVisible(!hidden);
            input.getMouse().showCursor(!hidden);
        }
    }
}
