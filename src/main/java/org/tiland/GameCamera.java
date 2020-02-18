package org.tiland;

import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.renderer.Window;
import org.engine.input.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Math;

public class GameCamera extends Camera {

    private static final float MOUSE_SENSITIVITY = 3.5f;
    private static final float PAN_SPEED = 5.0f;
    private static final float ZOOM_SPEED = 5.0f;

    Vector2f panVec;
    Vector2f scrollVec;

    Vector2f panSpeed;
    float panDrag = 16.0f;

    float zoomSpeed;
    float zoomDrag = 8.0f;

    Entity target = null;

    public GameCamera(Window window, Entity target) {

        super(window);

        this.target = target;

        setPosition(0.0f, 3.75f, 6.5f);

        panVec = new Vector2f();
        panSpeed = new Vector2f();

        scrollVec = new Vector2f();
    }

    private static boolean once = false;

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

        Keyboard keyboard = input.getKeyboard();

        panVec.zero();

     //   if (keyboard.keyDown(GLFW_KEY_A)){
       //     panVec.x = -1;
     //   } 

   //     if (keyboard.keyDown(GLFW_KEY_D)) {
     //       panVec.x = 1.0f;
       // }
/*
        if (keyboard.keyDown(GLFW_KEY_W)){
            panVec.y = 1;
        } 

        if (keyboard.keyDown(GLFW_KEY_S)) {
            panVec.y = -1.0f;
        }
*/
        if (panVec.length() > 0.0f) {
            panVec.normalize();
        }

        scrollVec.x = 0;
/*
        scrollVec.y = mouse.getScroll().y;
        if (scrollVec.y < 0) {
            scrollVec.y = -1;
        }

        if (scrollVec.y > 0) {
            scrollVec.y = 1;
        }
*/        

        if (input.getMouse().getShowCursor()) {
            return;
        }

    }

    @Override
    public void update(float interval) {

        /*
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

        if (zoomSpeed != 0.0f) {
            zoomSpeed *= 1.0f - zoomDrag * interval;
            
        }

        if (scrollVec.length() > 0.0f) {
            zoomSpeed = -scrollVec.y * ZOOM_SPEED;
        }
*/
        Vector3f pos = getPosition();
  
        pos.x = target.getPosition().x;

        // Apply panning.
        pos.add(panSpeed.x * interval, panSpeed.y * interval, 0.0f);

        // Apply zoom.
        pos.z += zoomSpeed * interval;
        if (pos.z < 5.0f) {
            pos.z = 5.0f;
        } else if (pos.z > 15.0f) {
            pos.z = 15.0f;
        }

      //  System.out.println(pos.z);
        setPosition(pos);

        super.update(interval);
    }
}
