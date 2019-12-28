import org.engine.renderer.Camera;
import org.engine.renderer.Window;
import org.engine.input.Mouse;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class GameCamera extends Camera {

    private Vector3f cameraMoveDir;

    private static final float MOUSE_SENSITIVITY = 30.0f;
    private static final float CAMERA_POS_STEP = 1.84f;

    public GameCamera() {
        cameraMoveDir = new Vector3f(0, 0, 0);

    }

    public void input(Window window, Mouse mouse) {

        cameraMoveDir.set(0, 0, 0);

        if ( window.isKeyPressed(GLFW_KEY_W) ) {
            cameraMoveDir.z = -1;
        }

        if ( window.isKeyPressed(GLFW_KEY_S) ) {
            cameraMoveDir.z = 1;
        }

        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraMoveDir.x = -1;
        }

        if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraMoveDir.x = 1;
        }

        if (cameraMoveDir.length() > 0.0f) {
            cameraMoveDir.normalize();
        }
    }

    public void update(float interval, Mouse mouse) {

        movePosition(cameraMoveDir.x * CAMERA_POS_STEP * interval, cameraMoveDir.y * CAMERA_POS_STEP * interval, cameraMoveDir.z * CAMERA_POS_STEP * interval);

        Vector2f rotVec = mouse.getDisplayVec();
        moveRotation(rotVec.x * MOUSE_SENSITIVITY * interval, rotVec.y * MOUSE_SENSITIVITY * interval, 0);

    }
}
