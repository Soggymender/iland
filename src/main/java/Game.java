import org.engine.*;

import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

import org.engine.input.*;
import org.engine.renderer.*;
import org.engine.resources.*;

import org.engine.Terrain;

public class Game implements IGame {

    private final SceneRenderer sceneRenderer;

    private final Avatar avatar;
    private final GameCamera camera;

    private Scene scene;

    private Hud hud;

    private float lightAngle;

    private static final float MOUSE_SENSITIVITY = 30.0f;

    private float accumulator = 0.0f;
    private float fpsTotal = 0.0f;
    private int   fpsSamples = 0;

    public Game()
    {
        sceneRenderer = new SceneRenderer();

        avatar = new Avatar();
        camera = new GameCamera(avatar);

        lightAngle = -90;
    }

    @Override
    public void initialize(Window window) throws Exception {
        sceneRenderer.initialize(window);

        scene = new Scene();

        float reflectance = 1.0f;

        avatar.initialize();
        scene.addEntities(avatar);

        avatar.setPosition(0.0f, -35.0f, 0.0f);

        /*
        Mesh[] mesh = StaticMeshLoader.load("src/main/resources/models/blender01.fbx", "src/main/resources/models/");
        Texture texture = mesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        mesh[0].setMaterial(material);

        float blockScale = 1.0f;

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
        */

        // Setup the terrain
        Vector3f terrainScale = new Vector3f(1000.0f, 200.0f, 1000.0f);
        int terrainSize = 3;
        float minY = 0.0f;//-0.1f;
        float maxY = 0.2f;//0.1f;
        int textInc = 40;

        Terrain terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "src/main/resources/textures/heightmap.png", "src/main/resources/textures/terrain.png", textInc);

        scene.setEntities(terrain.getEntities());


        // Setup  SkyBox
        float skyboxScale = 100.0f;
        Skybox skybox = new Skybox("src/main/resources/models/default_skybox.fbx", "src/main/resources/models/");
        skybox.setScale(skyboxScale);
        scene.setSkybox(skybox);


        // Setup Lights
        setupLights();

        camera.getPosition().x = 0.65f;
        camera.getPosition().y = 1.15f;
        camera.getPosition().y = 4.34f;

        hud = new Hud("0.0");
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

        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.shutdown();
        }

        hud.shutdown();
    }

    @Override
    public void input(Window window, Mouse mouse) {

        avatar.input(window, mouse);
        camera.input(window, mouse);
    }

    @Override
    public void update(float interval, Mouse mouse) {

        avatar.update(interval, mouse, camera);
        camera.update(interval, mouse);

        SceneLighting sceneLighting = scene.getSceneLighting();
        DirectionalLight directionalLight = sceneLighting.getDirectionalLight();

        // Update directional light direction, intensity and colour
        lightAngle += 3f * interval;
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

       if (accumulator >= 1.0f) {

           float fps = fpsTotal / fpsSamples;

           String fpsString = String.format("%.2f", fps);
           hud.setStatusText(fpsString);

           accumulator = 0.0f;
           fpsTotal = 0.0f;
           fpsSamples = 0;
       }

       float fps = 1.0f / interval;
       accumulator += interval;
       fpsTotal += fps;
       fpsSamples++;
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        sceneRenderer.render(window, camera, scene, hud);
    }
}