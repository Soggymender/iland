package org.tiland;

import org.engine.core.BoundingBox;
import org.engine.core.Math;
import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.renderer.Window;
import org.engine.input.*;
import org.joml.*;

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

        targetOffset = new Vector3f(0.0f, 2.45f, 250.0f);

        this.zone = zone;

        panVec = new Vector2f();
        panSpeed = new Vector2f();

        scrollVec = new Vector2f();

        bounds = new BoundingBox();

        bounds.min.x = -1;
        bounds.max.x =  1;
    }

    @Override
    public void input(Input input) {

        /*
        panVec.zero();

        if (panVec.length() > 0.0f) {
            panVec.normalize();
        }

        scrollVec.x = 0;

        if (input.getMouse().getShowCursor()) {
            return;
        }
        */

        //heading = heading + (float)input.getMouse().getScroll().y;

    }

    public void setHeading(float heading) {

        this.heading = heading;
    }

    public float getHeading() {
        return this.heading;
    }

    @Override
    public void update(float interval) {

        altUpdate(interval);
        return;

        /*
        Vector3f targetPos = target.getPosition();

        position.set(targetPos.x, targetOffset.y, targetPos.z);
        position.add(targetOffset.x, 0.0f, targetOffset.z);

        BoundingBox bounds = zone.getCameraBounds();

        // TODO: There's a bug here because frameVelocity will show a larger value than what was effectively applied.
        // But it should only matter if a collision happens that needs to be resolved while trying to pass the boundary.
        if (position.x < bounds.min.x -0.1f) {
            position.x = bounds.min.x -0.1f;
        }

        if (position.x > bounds.max.x + 0.1f) {
            position.x = bounds.max.x + 0.1f;
        }
*/
        /*
        if (heading != 0.0f) {

            if (heading < 0.0f) {

                heading = heading + interval * 10.0f;

                if (heading > 0.0f) {
                    heading = 0.0f;
                }
            } else {

                heading = heading - interval * 10.0f;

                if (heading < 0.0f) {
                    heading = 0.0f;
                }
            }
        }
        */
/*
        Vector3f followOffset = new Vector3f();

        followOffset.x = -targetPos.x;
        followOffset.y = 0.0f;
        followOffset.z = -targetPos.z;
        
        position.sub(followOffset);
        position.rotateY(Math.toRadians(heading));

//        followOffset = followOffset.negate();
        position.add(followOffset);
        rotation.y = -Math.toRadians(heading);

        super.update(interval);
*/
    }

    void altUpdate(float interval) {

        // Figure out the clipped X pos.

        Vector3f targetPos = target.getPosition();

        Vector3f clippedTargetPos = new Vector3f(targetPos);
        clippedTargetPos.y = 0.0f;
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

        clippedTargetPos.x += targetOffset.x;
        clippedTargetPos.y += targetOffset.y;
        position.set(clippedTargetPos);
        
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

        rotation.y = -Math.toRadians(heading);

        super.update(interval);
    }

    @Override
    public void updateViewMatrix() {

        viewMatrix.identity();
        
        viewMatrix.scale(0.1f);

        Vector3f targetPos = new Vector3f(target.getPosition());
        targetPos.y = 0.0f;
     
        Vector3f fromTarget = new Vector3f(position);
        fromTarget.sub(targetPos);


        viewMatrix.translate(0.0f, 0.0f, -targetOffset.z);
        viewMatrix.translate(-fromTarget.x, -fromTarget.y, -fromTarget.z);
        viewMatrix.rotate(-rotation.x, new Vector3f(1, 0, 0)).rotate(-rotation.y, new Vector3f(0, 1, 0));

        viewMatrix.translate(-targetPos.x, 0.0f, -targetPos.z);
        //viewMatrix.translate(-position.x, -position.y, -position.z);
    }    
}
