package org.tiland;

import java.awt.Font;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Rect;
import org.joml.Vector2f;

import org.engine.input.*;
import org.engine.renderer.Color;
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

    private Panel mapPanel;
    private Panel dialog;
    private Text dialogText;

    private Panel fade;
    private float curFadeValue = 0.0f;
    private float desFadeValue = 0.0f;
    private float fadeLength = 0.66f;
    private float fadeTime = 0.0f;

    private static final Font FONT = new Font("Arial", Font.BOLD, 16);
    private static final String CHARSET = "ISO-8859-1";

    private final Text fpsText;

    public Hud(Window window, Scene scene) throws Exception {

        FontTexture fontTexture = new FontTexture(FONT, CHARSET);

        canvas = new Canvas(window, new Vector2f(720, 480));
        
        fade = new Panel(canvas, canvas, new Rect(0, 0, 0, 0, true), new Rect(0, 0, 1, 1), new Vector2f(0, 0));

        mapPanel = new Panel(canvas, canvas, new Rect(-220, 20, 200, 100), new Rect(1, 0, 0, 0),       new Vector2f(0, 0));
     
        dialog = new Panel(canvas,  canvas, new Rect(20, 20, 400, 100), new Rect(0, 0, 0, 0),       new Vector2f(0, 0));
        dialogText = new Text(canvas, dialog, new Rect(5, 5, -5, 5), new Rect(0, 0, 1, 1), new Vector2f(0, 1), "0.0", fontTexture);;

        bPanel = new Panel(canvas,  canvas, new Rect(50, 50, -50, -50, true), new Rect(0, 0, 1, 1),       new Vector2f(0, 0));
        new Button(canvas, bPanel, new Rect(100, 100, 100, 30),      new Rect(0, 0, 0, 0),       new Vector2f(0, 0), "test words in a tiny button", fontTexture);
        new Panel(canvas,  bPanel, new Rect(-100, 100, 100, 50),     new Rect(1, 0, 1, 0, true), new Vector2f(1, 0));

        // Setup a text box.
        fpsText = new Text(canvas, canvas, new Rect(5, -5, 100, 20), new Rect(0, 1, 0, 1, true), new Vector2f(0, 1), "0.0", fontTexture);;
        fpsText.xJustifyCenter = false;

        fade.setVisible(false);
        fade.setDepth(1.0f);

        mapPanel.setVisible(true);
        mapPanel.setColor(new Color(0, 0, 0, 0.75f));
       mapPanel.setDepth(1.0f);

        dialog.setVisible(false);
        dialog.setColor(new Color(0, 0, 0, 0.75f));

        dialogText.xJustifyCenter = false;
        dialogText.yJustifyCenter = false;

        bPanel.setVisible(false);
        bPanel.setColor(new Color(0.25f, 0.0f, 0.75f, 0.5f));

        scene.addEntity(canvas);
    }

    public Canvas getCanvas() {
        return canvas;
    }
 
    public Rect getMapPanelRect() {
        return mapPanel.getScreenRect();
    }

    public void startFadeIn() {
        desFadeValue = 0.0f;
        fadeTime = 0.0f;
        fade.setVisible(true);
    }

    public void startFadeOut() {
        desFadeValue = 1.0f;
        fadeTime = 0.0f;
        fade.setVisible(true);
    }

    public void setFadeIn() {
        curFadeValue = 1.0f;
        desFadeValue = 0.0f;
        fade.setVisible(true);
    }

    public void setFadeOut() {
        curFadeValue = 1.0f;
        desFadeValue = 1.0f;
        fade.setVisible(false);
    }

    public void showDialog(boolean show) {
        dialog.setVisible(show);
    }

    public void setDialogText(String text) {
        dialogText.setText(text);
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

    public void update(float interval) {

        if (curFadeValue > desFadeValue) {
            // Fade in.
            fadeTime += interval;
            if (fadeTime > fadeLength) {
                fadeTime = fadeLength;
                fade.setVisible(false);
            }

            curFadeValue = 1 - fadeTime / fadeLength;       

        } else if (curFadeValue < desFadeValue) {
            // Fade out.
            fadeTime += interval;
            if (fadeTime >= fadeLength) {
                fadeTime = fadeLength;
                //fade.setVisible(false);
            }

            curFadeValue = fadeTime / fadeLength;
        }

        fade.setColor(new Color(0.0f, 0.0f, 0.0f, curFadeValue));
    }
}
