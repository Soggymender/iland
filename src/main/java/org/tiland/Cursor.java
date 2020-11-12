package org.tiland;

import org.engine.core.Transform;
import org.engine.renderer.Camera;
import org.engine.input.*;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Cursor extends Entity {

    Scene scene;
    Camera camera;

    public Cursor(Scene scene, Camera camera) {
        this.scene = scene;

        scene.addEntity(this);
    }

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

        if (mouse.leftButtonJustPressed()) {

            Vector2f pos = mouse.getPosition();
            Vector3f depthPos = new Vector3f(pos.x, pos.y, 0.1f);

            Vector3f nearPos = Transform.unproject(depthPos, camera);

            depthPos.z = 1.1f;
            Vector3f farPos = Transform.unproject(depthPos, camera);
        }
    }

    @Override
    public void update(float interval) {

    }


}