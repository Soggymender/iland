import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

import org.engine.IGame;
import org.engine.core.*;
import org.engine.input.*;
import org.engine.renderer.*;
import org.engine.resources.*;

public class Game implements IGame {

    private Vector3f cameraMoveDir;

    private final Renderer renderer;

    private final Camera camera;

    private Entity[] entities;

    private static final float MOUSE_SENSITIVITY = 8.4f;
    private static final float CAMERA_POS_STEP = 1.84f;

    public Game()
    {
        renderer = new Renderer();
        camera = new Camera();
        cameraMoveDir = new Vector3f(0, 0, 0);
    }

    @Override
    public void initialize(Window window) throws Exception {
        renderer.initialize(window);

//        Mesh mesh = OBJ.loadMesh("/models/blender01.obj");

  //      Texture texture = new Texture("src/main/resources/Textures/grassblock.png");

    //    mesh.setTexture(texture);

        //Mesh[] houseMesh = StaticMeshLoader.load("src/main/resources/models/house/house.obj", "src/main/resources/models/house");

        Mesh[] houseMesh = StaticMeshLoader.load("src/main/resources/models/blender01.fbx", "src/main/resources/models/");

        Entity entity = new Entity(houseMesh);
        entity.setScale(0.5f);
        entity.setPosition(0, 0, -2);
        entities = new Entity[] { entity };
    }

    @Override
    public void shutdown() {
        renderer.shutdown();

        for (Entity entity : entities) {
            entity.getMesh().shutdown();
        }
    }

    @Override
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

    @Override
    public void update(float interval, Mouse mouse) {

        camera.movePosition(cameraMoveDir.x * CAMERA_POS_STEP * interval, cameraMoveDir.y * CAMERA_POS_STEP * interval, cameraMoveDir.z * CAMERA_POS_STEP * interval);

         Vector2f rotVec = mouse.getDisplayVec();
         camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY * interval, rotVec.y * MOUSE_SENSITIVITY * interval, 0);
    }

    @Override
    public void render(Window window) {

        renderer.render(window, camera, entities);
    }
}