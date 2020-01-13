package org.engine.input;

import java.nio.DoubleBuffer;
import org.joml.Vector2d;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

import org.engine.renderer.Window;

public class Mouse {

    private Vector2d previousPos;

    private final Vector2d currentPos;

    private final Vector2f displVec;

    DoubleBuffer x;
    DoubleBuffer y;

    private boolean isActive = false;
    private boolean inWindow = false;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    public Mouse() {
        isActive = false;
        previousPos = new Vector2d(0, 0);
        currentPos = new Vector2d(0, 0);
        displVec = new Vector2f();

        x = org.lwjgl.BufferUtils.createDoubleBuffer(1);
        y = org.lwjgl.BufferUtils.createDoubleBuffer(1);

    }

    public void initialize(Window window) {

//        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
//        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

//        glfwSetInputMode(window.getWindowHandle(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);

        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {




  //          currentPos.x = xpos;
    //        currentPos.y = ypos;

           // if (!isActive) {
               // previousPos = currentPos;
             //   isActive = true;
            //}
        });
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            inWindow = entered;
        });
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public void shutdown(Window window) {
    //    glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    }

    public Vector2f getDisplayVec() {
        return displVec;
    }

    public void input(Window window) {




        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;

        x.clear();
        y.clear();

        glfwGetCursorPos(window.getWindowHandle(), x, y);

        x.rewind();
        y.rewind();

        currentPos.x = x.get();
        currentPos.y = y.get();

        if (!isActive) {
            isActive = true;
            System.out.println("crap");
            return;
        }


        displVec.x = 0;
        displVec.y = 0;

        double deltax = currentPos.x - previousPos.x;
        double deltay = currentPos.y - previousPos.y;
        boolean rotateX = deltax != 0;
        boolean rotateY = deltay != 0;
        if (rotateX) {
            displVec.y = (float) deltax;
        }
        if (rotateY) {
            displVec.x = (float) deltay;
        }
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}
