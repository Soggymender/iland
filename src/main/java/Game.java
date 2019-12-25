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

    private Vector3f ambientLight;
    private PointLight pointLight;

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

        Mesh[] mesh = StaticMeshLoader.load("src/main/resources/models/blender01.fbx", "src/main/resources/models/");

        Texture texture = mesh[0].getMaterial().getTexture();

        Material material = new Material(texture, 1.0f);

        mesh[0].setMaterial(material);

        Entity entity = new Entity(mesh);
        entity.setScale(0.5f);
        entity.setPosition(0, 0, -2);
        entities = new Entity[] { entity };

        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
        Vector3f lightColor = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntesnity = 1.0f;

        pointLight = new PointLight(lightColor, lightPosition, lightIntesnity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
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

        float lightPos = pointLight.getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.pointLight.getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.pointLight.getPosition().z = lightPos - 0.1f;
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

        renderer.render(window, camera, entities, ambientLight, pointLight);
    }
}