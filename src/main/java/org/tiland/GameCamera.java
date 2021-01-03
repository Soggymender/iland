package org.tiland;

import org.engine.core.BoundingBox;
import org.engine.core.Math;
import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.renderer.Window;
import org.engine.input.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

import javax.lang.model.util.ElementScanner6;

public class GameCamera extends Camera {

    Zone zone = null;

    Vector2f panVec;
    Vector2f scrollVec;

    Vector2f panSpeed;
    float panDrag = 16.0f;

    float zoomSpeed;
    float zoomDrag = 8.0f;

    Entity target = null;
    Vector3f targetOffset = null;
    BoundingBox bounds;

    float heading = 0.0f;

    public GameCamera(Window window, Entity target, Zone zone) {

        super(window);

        this.target = target;

//        targetOffset = new Vector3f(0.0f, 2.45f, 4.25f);
        targetOffset = new Vector3f(0.0f, 0.75f, 4.25f);

        this.zone = zone;

        panVec = new Vector2f();
        panSpeed = new Vector2f();

        scrollVec = new Vector2f();

        bounds = new BoundingBox();

        bounds.min.x = -5;
        bounds.max.x =  5;
    }

    @Override
    public void input(Input input) {

//        if (input.getKeyboard().keyDown(GLFW_KEY_C))
  //          heading = heading + (float)input.getMouse().getScroll().y;

    }

    public void setHeading(float heading) {

        this.heading = heading;
    }

    public float getHeading() {
        return this.heading;
    }

    @Override
    public void update(float interval) {

        // Figure out the clipped X pos.

        Vector3f targetPos = target.getPosition();

        Vector3f clippedTargetPos = new Vector3f(targetPos);
        //clippedTargetPos.y = 0.0f;
        clippedTargetPos.z = 0.0f;

        BoundingBox bounds = zone.getCameraBounds();

        // TODO: There's a bug here because frameVelocity will show a larger value than what was effectively applied.
        // But it should only matter if a collision happens that needs to be resolved while trying to pass the boundary.
        if (clippedTargetPos.x < bounds.min.x -0.1f) {
            clippedTargetPos.x = bounds.min.x -0.1f;
        }
        if (clippedTargetPos.x > bounds.max.x + 0.1f) {
            clippedTargetPos.x = bounds.max.x + 0.1f;
        }

        // Try some fancy Y clipping.
        if (clippedTargetPos.y < bounds.min.y + 0.9f) {
            clippedTargetPos.y = bounds.min.y + 0.9f;
        }
        if (clippedTargetPos.y > bounds.max.y - 4.0f) {
            clippedTargetPos.y = bounds.max.y - 4.0f;
        }

        clippedTargetPos.x += targetOffset.x;
        clippedTargetPos.y += targetOffset.y;
        position.set(clippedTargetPos);
        
        /*
        if (heading != 0.0f) {

            if (heading < 0.0f) {

                heading = heading + interval * 75.0f;

                if (heading > 0.0f) {
                    heading = 0.0f;
                }
            } else {

                heading = heading - interval * 75.0f;

                if (heading < 0.0f) {
                    heading = 0.0f;
                }
            }
        }
        */
        
        if (zone.transition.headingTransition()) {

            float p = zone.transition.getTransitionPercent();

            rotation.y = -Math.toRadians(heading * (1.0f - p));
        } else
            rotation.y = 0.0f;
    //    rotation.y = -Math.toRadians(heading);
    
        super.update(interval);
    }

    @Override
    public void updateViewMatrix() {

        viewMatrix.identity();        
        
        viewMatrix.scale(150.0f, 150.0f, 1.0f);

        Vector3f targetPos = new Vector3f(target.getPosition());
        targetPos.y = 0.0f;
     
        Vector3f fromTarget = new Vector3f(position);
        fromTarget.sub(targetPos);


        viewMatrix.translate(0.0f, -targetOffset.y, -targetOffset.z);
        viewMatrix.translate(-fromTarget.x, -fromTarget.y, -fromTarget.z);
        viewMatrix.rotate(-rotation.x, new Vector3f(1, 0, 0)).rotate(-rotation.y, new Vector3f(0, 1, 0));

        viewMatrix.translate(-targetPos.x, -targetPos.y, -targetPos.z);
        //viewMatrix.translate(-position.x, -position.y, -position.z);

    }    
}
