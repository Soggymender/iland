package org.niland;

import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.renderer.Window;
import org.engine.input.*;
import org.joml.*;

import org.engine.core.Math;

public class GameCamera extends Camera {

    private Vector2f rotVec;

    private Vector3f followPivot;
    private Vector3f followOffset;
    private Vector3f followRotation;

    private Entity target;
    private float zoom = 0.0f;

    private static final float MOUSE_SENSITIVITY = 0.3f;

    public GameCamera(Window window, Entity target) {

        super(window);

        setViewportEx(0.1f, 100.0f, false);

        rotVec = new Vector2f();

        this.target = target;

        followPivot = new Vector3f(0.0f, 0.5f, 0.0f);
        followOffset = new Vector3f(0.0f, 0.0f, 5.0f);
        followRotation = new Vector3f(0.0f, 0.0f, 0.0f);

     //   setViewport(0, 0, 640, 480, true);
    }

    private static boolean once = false;

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

        Vector2f scroll = mouse.getScroll();

        zoom += scroll.y / 50.0f;
        zoom = java.lang.Math.max(0.0f, java.lang.Math.min(1.0f, zoom));


        if (input.getMouse().getShowCursor()) {
            rotVec.zero();
            return;
        }

        rotVec = mouse.getDisplayVec();

        rotVec.x *= MOUSE_SENSITIVITY;
        rotVec.y *= MOUSE_SENSITIVITY;
    }

    @Override
    public void update(float interval) {

        followRotation.x += rotVec.x * interval;
        followRotation.y += rotVec.y * interval;

        // Cap look up & down.
        followRotation.x = java.lang.Math.max(followRotation.x, Math.toRadians(-90.0f));
        followRotation.x = java.lang.Math.min(followRotation.x, Math.toRadians(90.0f));

        if (!once) {
            follow(interval);
        }

        super.update(interval);
    }

    private void follow(float interval) {

        if (target == null) {
            return;
        }

        Vector3f zoomedFollowOffset = new Vector3f(followOffset);
        zoomedFollowOffset.mul(1.0f - zoom);

        position.set(zoomedFollowOffset);
        position.rotateX(followRotation.x);
        position.rotateY(followRotation.y);

        Vector3f targetPos = new Vector3f(target.getPosition());

        position.add(followPivot);
        position.add(targetPos);

        // Look at the target.
        rotation.x = followRotation.x;
        rotation.y = followRotation.y;
    }
}
