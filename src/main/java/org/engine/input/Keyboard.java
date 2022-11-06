package org.engine.input;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;

import java.util.Arrays;

import org.engine.renderer.Window;

public class Keyboard {

    Window window = null;

    boolean[] oldKeyStates = new boolean[GLFW_KEY_LAST];
    boolean[] keyStates = new boolean[GLFW_KEY_LAST];

    GLFWKeyCallback keyCallback = null;

    public Keyboard(Window window) {
        this.window = window;

        Arrays.fill(oldKeyStates, false);
        Arrays.fill(keyStates, false);

        glfwSetInputMode(window.getWindowHandle(), GLFW_STICKY_KEYS, GLFW_TRUE);

//        glfwSetKeyCallback(window.getWindowHandle(), keyCallback = GLFWKeyCallback.create((windowHandle, key, scancode, action, mods) -> {
//        }));        
    }

    public void input() {

        // Copy the previous state of the keys.
        System.arraycopy(keyStates, 0, oldKeyStates, 0, GLFW_KEY_LAST);

        long windowHandle = window.getWindowHandle();

        for (int i = 32; i < GLFW_KEY_LAST; i++) {
            keyStates[i] = glfwGetKey(windowHandle, i) == GLFW_PRESS;
        }
    }

    public boolean keyDown(int key) {
        return keyStates[key];
    }

    public boolean keyJustDown(int key) {
        return keyStates[key] && !oldKeyStates[key];
    }

    public boolean keyJustUp(int key) {
        return !keyStates[key] && oldKeyStates[key];
    }
}
