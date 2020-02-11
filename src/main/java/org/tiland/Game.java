package org.tiland;

import java.util.List;
import java.util.Map;

import org.engine.scene.*;
import org.joml.Vector3f;

import org.engine.input.*;
import org.engine.renderer.*;

import org.tiland.TileMap;

public class Game {

    private final GameCamera camera;
    private Entity cameraTarget;

    private Scene scene = null;

    private Hud hud;

    private Cursor cursor;

    private float accumulator = 0.0f;
    private float fpsTotal = 0.0f;
    private int   fpsSamples = 0;

    private TileMap tileMap = null;

    public Game(Window window, Scene scene) throws Exception
    {
        this.scene = scene;

        cameraTarget = new Entity();
        cameraTarget.setPosition(0, 0,0);
    
        camera = new GameCamera(window, cameraTarget);
     
        scene.setCamera(camera);

        hud = new Hud(window, scene);

        cursor = new Cursor(scene);

        tileMap = new TileMap(scene);
    }

    public void initialize() throws Exception {

        // Setup Lights
        setupLights();
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
}