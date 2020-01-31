import org.engine.scene.Entity;
import org.engine.renderer.Camera;
import org.engine.input.*;
import org.joml.*;

import org.engine.core.Math;

public class GameCamera extends Camera {

    private Vector2f rotVec;

    private Vector3f followPivot;
    private Vector3f followOffset;
    private Vector3f followRotation;

    private Entity target;

    private static final float MOUSE_SENSITIVITY = 3.5f;

    public GameCamera(Entity target) {

        rotVec = new Vector2f();

        this.target = target;

        followPivot = new Vector3f(0.0f, 2.0f, 0.0f);
        followOffset = new Vector3f(0.0f, 0.0f, 5.0f);
        followRotation = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    private static boolean once = false;

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

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
        followRotation.x = java.lang.Math.max(followRotation.x, Math.toRadians(-45.0f));
        followRotation.x = java.lang.Math.min(followRotation.x, Math.toRadians(45.0f));

        if (!once) {
            follow(interval);
        }
    }

    private void follow(float interval) {

        if (target == null) {
            return;
        }

        position.set(followOffset);
        position.rotateX(-followRotation.x);
        position.rotateY(-followRotation.y);

        Vector3f targetPos = new Vector3f(target.getPosition());

        position.add(followPivot);
        position.add(targetPos);

        // Look at the target.
        rotation.x = followRotation.x;
        rotation.y = followRotation.y;
    }
}
