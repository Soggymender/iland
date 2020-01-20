package org.engine.input;

import org.engine.renderer.Window;

public class Keyboard {

    Window window = null;

    public Keyboard(Window window) {
        this.window = window;
    }

    public void input() {

    }

    public boolean keyDown(int key) {
        return window.isKeyPressed(key);
    }
}
