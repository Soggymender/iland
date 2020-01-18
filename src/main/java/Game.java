import org.engine.*;

import java.util.List;
import java.util.Map;

import org.engine.scene.*;
import org.joml.Vector3f;

import org.engine.input.*;
import org.engine.renderer.*;

import org.engine.Terrain;

public class Game implements SceneLoader.IEventHandler {

    private final Avatar avatar;
    private final GameCamera camera;

    Terrain terrain;

    private Scene scene = null;

    private Hud hud;

    private float lightAngle;

    private static final float MOUSE_SENSITIVITY = 30.0f;

    private float accumulator = 0.0f;
    private float fpsTotal = 0.0f;
    private int   fpsSamples = 0;

    public Game(Scene scene)
    {
        this.scene = scene;

        avatar = new Avatar();
        camera = new GameCamera(avatar);

        scene.setCamera(camera);

        lightAngle = -45;
    }

    public void initialize(Window window) throws Exception {

        float reflectance = 1.0f;

        avatar.initialize();
        scene.addEntityMeshes(avatar);

        avatar.setPosition(0.0f, 0.0f, 0.0f);


        // Load entities from FBX - their types specified via Blender custom properties.
        // Manually add each to the scene.
        // Afterward, programatically add other entities to the scene.
        SceneLoader.loadEntities("src/main/resources/models/terrain_mesh_test.fbx", "src/main/resources/models/", this);

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

        hud = new Hud(window, scene);
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

    public void shutdown() {

        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.shutdown();
        }

        hud.shutdown();
    }

    public void input(Window window, Mouse mouse) {
        avatar.input(window, mouse);
    }

    public void update(float interval, Mouse mouse) {

        avatar.update(interval, mouse, camera, terrain);

        SceneLighting sceneLighting = scene.getSceneLighting();
        DirectionalLight directionalLight = sceneLighting.getDirectionalLight();

        // Update directional light direction, intensity and colour
        //lightAngle += 3f * interval;
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

    public void render(Window window) {
        hud.updateSize(window);
    }

    public Entity preLoadEntityEvent(String type) throws Exception {

        if (type.compareTo("terrain") == 0) {

            // Create a terrain entity.
            terrain = new Terrain();
            return terrain;
        }

        return null;
   }

    public void postLoadEntityEvent(Entity entity) throws Exception {

        if (entity instanceof Terrain) {

            Terrain terrainEntity = (Terrain)entity;

            Mesh mesh = terrainEntity.getMesh();
            terrainEntity.clearMeshes();

            // Specify the textures since they can't be specified in the scene FBX. Yuck.
            terrainEntity.createFromMesh(mesh, "src/main/resources/textures/terrain.png");
            scene.addEntitiesMeshes(terrainEntity.getEntities());
        }

        // Add these entities to the scene.
        scene.addEntityMeshes(entity);
    }
}