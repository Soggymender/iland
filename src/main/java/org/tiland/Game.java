package org.tiland;

import java.util.List;
import java.util.Map;

import org.engine.scene.*;
import org.joml.Vector3f;

import org.engine.input.*;
import org.engine.renderer.*;
import org.engine.Utilities;

public class Game implements SceneLoader.IEventHandler {

    private Zone zone;

    private Avatar avatar;
    private final GameCamera camera;

    private Scene scene = null;
    private Scene mapScene = null;

    private SceneRenderer sceneRenderer = null;

    private Hud hud;

    private MiniMap map;

    private float accumulator = 0.0f;
    private float fpsTotal = 0.0f;
    private int   fpsSamples = 0;

    Shader tileShader = null;

    int count = 0;

    //private TileMap tileMap = null;

    public Game(Window window, Scene scene, Scene mapScene, SceneRenderer sceneRenderer) throws Exception
    {
        this.scene = scene;
        this.mapScene = mapScene;

        this.sceneRenderer = sceneRenderer;

        zone = new Zone(scene, this);

        avatar = new Avatar(scene, zone);

        camera = new GameCamera(window, avatar, zone);

        hud = new Hud(window, scene);

        // The target is actually the main camera since it doesn't track during jumps, etc.
        map = new MiniMap(mapScene, camera, hud, window);
    }

    public void initialize() throws Exception {

        initializeTileShader();

        zone.requestZone("temple", "");

        // Setup Lights
        setupLights();
    }

    private void startZone() {

        hud.setFadeOut();
        hud.startFadeIn();

        zone.loadRequestedZone();

        avatar.goToStart();
        
        if (zone.enteredByDoor()) {
            if (count % 2 != 0) {
                camera.setHeading(45.0f);
            } else {
                camera.setHeading(-45.0f);
            }
        }
        
        count += 1;

        //mapScene.addEntity(zone.zoneRoot);
        map.addZone(zone.getName(), zone.getMapOffset(), zone.getMapHeading(), zone.getAvatarBounds());
        map.enterZone(zone.getName());
    }

    private void initializeTileShader() throws Exception {

        ShaderCache shaderCache = ShaderCache.getInstance();
        tileShader = shaderCache.addShader("tile");

        String vsName = Utilities.load("/shaders/tile_vertex.vs");
        String fsName = Utilities.load("/shaders/tile_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        tileShader.createVertexShader(vsName);
        tileShader.createFragmentShader(fsName);
        tileShader.link();

        TileUniformManager uniformManager = new TileUniformManager(tileShader);
        tileShader.setUniformManager(uniformManager);
    }

    private void setupLights() {
        SceneLighting sceneLighting = new SceneLighting();
        scene.setSceneLighting(sceneLighting);
        mapScene.setSceneLighting(sceneLighting);

        // Ambient Light
     //   sceneLighting.setAmbientLight(new Vector3f(0.5f, 0.5f, 0.5f));

        // Directional Light
        float lightIntensity = 0.75f;
        Vector3f lightPosition = new Vector3f(0.0f, 0, 1);
        sceneLighting.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));



    }

    public void shutdown() {

        // TODO: Let the scene system handle this.
        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.shutdown();
        }
    }

    public void input(Input input) {
        if (!input.getMouse().getShowCursor()){

        }
       
        hud.input(input);

        scene.input(input);
    }

    public void update(float interval) {

        // Check for a zone request.
        if (!zone.getRequestedZone().isEmpty()) {
            startZone();
        }

        hud.update(interval);

        map.update(interval);

        if (accumulator >= 1.0f) {

            float fps = 0;
            if (fpsSamples > 0) {
                fps = fpsTotal / fpsSamples;
            }

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

       scene.update(interval);
       mapScene.update(interval);
    }

    public void render(float interval) {

        // Update the camera last so that the targets transform is up to date and already simulated.
        camera.update(interval);
        map.getCamera().update(interval);

        // Render
        sceneRenderer.render(camera, scene, true);
        sceneRenderer.render(map.getCamera(), map.getScene(), false);
    }

    public void LoadSRequestEvent() {

    }

    public Entity preLoadEntityEvent(Map<String, String>properties) {

        String type = properties.get("p_type");
        if (type != null) {

            if (type.equals("door")) {
                return zone.createDoor(properties);
            }

        } else {

            String collision = properties.get("p_collision");
            if (collision != null) {
                if (collision.equals("platform")) {

                } else if (collision.equals("box")) {
                    System.out.println("pre box collision");
                }
            }

            String depth = properties.get("p_depth");
            if (depth != null) {
                Tile tile = new Tile();
                tile.depth = Float.parseFloat(depth);

                return tile;
            } 
        }

        return new Entity();
    }

    public void postLoadEntityEvent(Entity entity, Map<String, String>properties) {

        String avatarStart = properties.get("p_avatar_start");
        if (avatarStart != null) {
            zone.setAvatarStart(entity.getPosition());
            return;
        }

        Mesh mesh = entity.getMesh();
        if (mesh != null) {
            Material material = mesh.getMaterial();
            if (material != null) {
                material.setShader(tileShader);
            }
        }

        if (!(entity instanceof Door)) {
            zone.addEntity(entity);
        }

        String collision = properties.get("p_collision");
        if (collision != null) {
            entity.flags.collidable = true;

            if (collision.equals("platform")) {
                entity.flags.platform_collision = true;
            } else if (collision.equals("box")) {
                entity.flags.box_collision = true;
                System.out.println("box collision");
            }
        }
    }
}