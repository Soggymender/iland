package org.tiland;

import org.engine.renderer.*;
import org.engine.scene.*;

public class MiniMap {
 
    private Scene scene;
    private Entity target;

    public Camera camera;

    public MiniMap(Scene scene, Entity target, Window window) {

        this.scene = scene;
        this.target = target;

        camera = new Camera();
                
        camera.setPosition(0,2.5f, 10.0f);

        float windowWidth = window.getWidth();
        float windowHeight = window.getHeight();

        float x = 0 + (windowWidth * 0.5f / 2.0f);
        float y = 0 + (windowHeight * 0.5f / 2.0f);
        float width = windowWidth * 0.5f;
        float height = windowHeight * 0.5f;

        camera.setViewport(x, y, width, height);
    }

    public void update(float interval) {




    }
}