package org.engine.input;

import org.engine.renderer.Window;

public class Input {

    private Mouse mouse = null;
    private Keyboard keyboard = null;

    public Input(Mouse mouse, Keyboard keyboard) {

        this.mouse = mouse;
        this.keyboard = keyboard;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public void input() {

        mouse.input();
        keyboard.input();
    }
}
