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

        targetOffset = new Vector3f(0.0f, 2.45f, 4.25f);

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

        panVec.zero();

        if (panVec.length() > 0.0f) {
            panVec.normalize();
        }

        scrollVec.x = 0;

        if (input.getMouse().getShowCursor()) {
            return;
        }

    }

    public void setHeading(float heading) {

        this.heading = heading;
    }

    @Override
    public void update(float interval) {

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

        if (heading != 0.0f) {

            if (heading < 0.0f) {

                heading = heading + interval * 100.0f;

                if (heading > 0.0f) {
                    heading = 0.0f;
                }
            } else {

                heading = heading - interval * 100.0f;

                if (heading < 0.0f) {
                    heading = 0.0f;
                }
            }
        }

        Vector3f followOffset = new Vector3f();

        followOffset.x = -targetPos.x;
        followOffset.y = 0.0f;
        followOffset.z = -targetPos.z;
        
        position.add(followOffset);
        position.rotateY(-Math.toRadians(heading));

        followOffset = followOffset.negate();
        position.add(followOffset);
        rotation.y = Math.toRadians(heading);

        super.update(interval);
    }
}
