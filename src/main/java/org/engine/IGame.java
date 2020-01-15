package org.engine;

import org.engine.input.*;
import org.engine.renderer.Window;

public interface IGame {

    void initialize(Window window) throws Exception;

    void input(Window window, Mouse mouse, float interval);

    void update(float interval, Mouse mouse);

    void render(Window window);

    void shutdown();
}