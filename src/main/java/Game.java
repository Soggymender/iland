import org.engine.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

import org.engine.input.*;
import org.engine.renderer.*;
import org.engine.resources.*;

public class Game implements IGame {

    private Vector3f cameraMoveDir;

    private final SceneRenderer sceneRenderer;
    private final Camera camera;

    private Scene scene;

    private Hud hud;

    private float lightAngle;

    private static final float MOUSE_SENSITIVITY = 8.4f;
    private static final float CAMERA_POS_STEP = 1.84f;

    public Game()
    {
        sceneRenderer = new SceneRenderer();
        camera = new Camera();
        cameraMoveDir = new Vector3f(0, 0, 0);

        lightAngle = -90;
    }

    @Override
    public void initialize(Window window) throws Exception {
        sceneRenderer.initialize(window);

        scene = new Scene();

        float reflectance = 1.0f;

        Mesh[] mesh = StaticMeshLoader.load("src/main/resources/models/blender01.fbx", "src/main/resources/models/");
        Texture texture = mesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        mesh[0].setMaterial(material);

        float blockScale = 0.5f;
        float skyboxScale = 15.0f;
        float extension = 2.0f;

        float startx = extension * (-skyboxScale + blockScale);
        float startz = extension * (skyboxScale - blockScale);
        float starty = -1.0f;
        float inc = blockScale * 2;

        float posx = startx;
        float posz = startz;
        float incy = 0.0f;
        int NUM_ROWS = (int)(extension * skyboxScale * 2 / inc);
        int NUM_COLS = (int)(extension * skyboxScale * 2/ inc);
        Entity[] entities  = new Entity[NUM_ROWS * NUM_COLS];
        for(int i=0; i<NUM_ROWS; i++) {
            for(int j=0; j<NUM_COLS; j++) {
                Entity entity = new Entity(mesh);
                entity.setScale(blockScale);
                incy = Math.random() > 0.9f ? blockScale * 2 : 0f;
                entity.setPosition(posx, starty + incy, posz);
                entities[i*NUM_COLS + j] = entity;

                posx += inc;
            }
            posx = startx;
            posz -= inc;
        }
        scene.setEntities(entities);


        // Setup  SkyBox

        Skybox skybox = new Skybox("src/main/resources/models/default_skybox.fbx", "src/main/resources/models/");
        skybox.setScale(skyboxScale);
        scene.setSkybox(skybox);


        // Setup Lights
        setupLights();

        camera.getPosition().x = 0.65f;
        camera.getPosition().y = 1.15f;
        camera.getPosition().y = 4.34f;

        hud = new Hud("text");
    }

    private void setupLights() {
        SceneLighting sceneLighting = new SceneLighting();
        scene.setSceneLighting(sceneLighting);

        // Ambient Light
        sceneLighting.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(-1, 0, 0);
        sceneLighting.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));
    }

    @Override
    public void shutdown() {
        sceneRenderer.shutdown();
        Entity[] entities = scene.getEntities();
        for (Entity entity : entities) {
            entity.getMesh().shutdown();
        }

        hud.shutdown();
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

        SceneLighting sceneLighting = scene.getSceneLighting();
        DirectionalLight directionalLight = sceneLighting.getDirectionalLight();

        // Update directional light direction, intensity and colour
        lightAngle += 1.1f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        sceneRenderer.render(window, camera, scene, hud);
    }
}