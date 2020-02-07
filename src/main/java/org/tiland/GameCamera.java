package org.tiland;

import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.input.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Math;

public class GameCamera extends Camera {

    private static final float MOUSE_SENSITIVITY = 3.5f;
    private static final float PAN_SPEED = 5.0f;
    private static final float ZOOM_SPEED = 1.0f;

    Vector2f panVec;

    Vector2f panSpeed;
    float panDrag = 16.0f;

    public GameCamera(Entity target) {

        setPosition(0.0f, 0.0f, 5.0f);

        panVec = new Vector2f();
        panSpeed = new Vector2f();
    }

    private static boolean once = false;

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

        Keyboard keyboard = input.getKeyboard();

        panVec.zero();

        if (keyboard.keyDown(GLFW_KEY_A)){
            panVec.x = -1;
        } 

        if (keyboard.keyDown(GLFW_KEY_D)) {
            panVec.x = 1.0f;
        }

        if (keyboard.keyDown(GLFW_KEY_W)){
            panVec.y = 1;
        } 

        if (keyboard.keyDown(GLFW_KEY_S)) {
            panVec.y = -1.0f;
        }

        if (panVec.length() > 0.0f) {
            panVec.normalize();
        }

        if (input.getMouse().getShowCursor()) {
            return;
        }

    }

    @Override
    public void update(float interval) {

        float panLength = panSpeed.length();
        if (panLength > 0.0f) {
            panLength -= panDrag * interval;
            if (panLength < 0.0f) {
                panLength = 0.0f;
            }

            panSpeed.normalize();
            panSpeed.mul(panLength);
        }

        if (panVec.length() > 0.0f) {
            panSpeed.x = panVec.x * PAN_SPEED;
            panSpeed.y = panVec.y * PAN_SPEED;
        }

        Vector3f pos = getPosition();
        pos.add(panSpeed.x * interval, panSpeed.y * interval, 0.0f);

        setPosition(pos);


    }
}
