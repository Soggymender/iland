package org.tiland;

import java.util.List;
import java.util.Map;

import org.engine.scene.*;
import org.joml.Vector3f;

import org.engine.input.*;
import org.engine.renderer.*;
import org.engine.Utilities;

import org.tiland.Tile;
import org.tiland.TileUniformManager;

public class Game implements SceneLoader.IEventHandler {

    private Avatar avatar;
    private final GameCamera camera;

    private Scene scene = null;

    private Hud hud;

    private float accumulator = 0.0f;
    private float fpsTotal = 0.0f;
    private int   fpsSamples = 0;

    Shader tileShader = null;

    //private TileMap tileMap = null;

    public Game(Window window, Scene scene) throws Exception
    {
        this.scene = scene;

        avatar = new Avatar(scene);

        camera = new GameCamera(window, avatar);
     
        scene.setCamera(camera);

        hud = new Hud(window, scene);
    }

    public void initialize() throws Exception {

        initializeTileShader();

        SceneLoader.loadEntities("src/main/resources/tiland/models/temple.fbx", "src/main/resources/tiland/textures/", this);

        // Setup Lights
        setupLights();

       
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

        // Ambient Light
     //   sceneLighting.setAmbientLight(new Vector3f(0.5f, 0.5f, 0.5f));

        // Directional Light
        float lightIntensity = 0.5f;
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
    }

    public void update(float interval) {

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
    }

    public Entity preLoadEntityEvent(Map<String, String>properties) throws Exception {

        String depth = properties.get("p_depth");
        if (depth != null) {
            Tile tile = new Tile();
            tile.depth = Float.parseFloat(depth);

            return tile;
        } 
        
        return new Entity();

    }

    public void postLoadEntityEvent(Entity entity, Map<String, String>properties) throws Exception {

        Mesh mesh = entity.getMesh();
        if (mesh != null) {
            Material material = mesh.getMaterial();
            if (material != null) {
                material.setShader(tileShader);
            }
        }

        scene.addEntity(entity);

        String collision = properties.get("p_collision");
        if (collision != null) {
            entity.flags.collidable = true;

            if (collision == "platform") {
                entity.flags.platform_collision = true;
            } else if (collision == "box") {
                entity.flags.box_collision = true;
            }
        }
    }
}