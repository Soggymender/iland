package org.engine.input;

import java.nio.DoubleBuffer;
import org.joml.Vector2d;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWScrollCallback;

import org.engine.renderer.Window;

public class Mouse {

    private Window window = null;

    private Vector2d previousPos;
    private final Vector2d currentPos;

    private final Vector2f displVec;

    private Vector2f pendingScroll;
    private Vector2f scroll;

    DoubleBuffer x;
    DoubleBuffer y;

    private boolean show;

    private boolean isActive = false;

    private boolean prevLeftButtonPressed = false;
    private boolean prevRightButtonPressed = false;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    GLFWScrollCallback scrollCallback;

    public Mouse(Window window) {

        this.window = window;

        isActive = false;
        previousPos = new Vector2d(0, 0);
        currentPos = new Vector2d(0, 0);
        displVec = new Vector2f();

        pendingScroll = new Vector2f();
        scroll = new Vector2f();

        x = org.lwjgl.BufferUtils.createDoubleBuffer(1);
        y = org.lwjgl.BufferUtils.createDoubleBuffer(1);

        initialize();

    }

    /*public void glfwScrollCallback(long window, double xoffset, double yoffset) {

    }
    */

    public void initialize() {

        showCursor(false);

        glfwSetInputMode(window.getWindowHandle(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);

        /*
        Turns out that while GLFW lets you poll much of the input, but there is critical data that is not
        available by polling, so no matter what, callbacks are necessary.

        In the callbacks below I'll attempt to accumulate any input that comes in but not give the game direct
        access to it. Instead, during this::input, the accumulated callback input will be pushed to game-exposed
        variables for polling by the game, and the accumulated values cleared out.

        This is necessary because the callbacks occur outside of the preferred engine sequencing.
        */

        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {

        });

        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {

        });

        glfwSetScrollCallback(window.getWindowHandle(), (windowHandle, xOffset, yOffset) -> {

            pendingScroll.x += (float)xOffset;
            pendingScroll.y += (float)yOffset;

        });
    }

    public void shutdown() {
    //    glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    }



    public Vector2f getDisplayVec() {
        return displVec;
    }

    public Vector2f getPosition() {

        Vector2f pos = new Vector2f();
        pos.x = (float)currentPos.x;
        pos.y = (float)currentPos.y;

        return pos;
    }

    public Vector2f getScroll(){
        return scroll;
    }

    public void input() {

        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;

        x.clear();
        y.clear();

        glfwGetCursorPos(window.getWindowHandle(), x, y);

        x.rewind();
        y.rewind();

        currentPos.x = x.get();
        currentPos.y = y.get();

        scroll.x = pendingScroll.x;
        scroll.y = pendingScroll.y;

        pendingScroll.zero();    

        if (!isActive) {
            isActive = true;
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


        prevLeftButtonPressed = leftButtonPressed;
        prevRightButtonPressed = rightButtonPressed;

        leftButtonPressed = glfwGetMouseButton(window.getWindowHandle(), GLFW_MOUSE_BUTTON_1) != 0;
        rightButtonPressed = glfwGetMouseButton(window.getWindowHandle(), GLFW_MOUSE_BUTTON_2) != 0;
    }

    public void showCursor(boolean show) {
      
        this.show = show;

        if (show) {
            
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }

    }

    public boolean getShowCursor() {
        return show;
    }

    public boolean leftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean rightButtonPressed() {
        return rightButtonPressed;
    }

    public boolean leftButtonJustPressed() {
        
        return leftButtonPressed && !prevLeftButtonPressed;
     }

    public boolean rightButtonJustPressed() {
         return rightButtonPressed && !prevRightButtonPressed;
     }
}
