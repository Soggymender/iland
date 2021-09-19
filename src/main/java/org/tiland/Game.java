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
    private float fps = 0;

    Shader tileShader = null;

    int count = 0;

    DirectionalLight directionalLight;

    float heading;

    //private TileMap tileMap = null;

    public Game(Window window, Scene scene, Scene mapScene, SceneRenderer sceneRenderer) throws Exception
    {
        this.scene = scene;
        this.mapScene = mapScene;

        this.sceneRenderer = sceneRenderer;

        hud = new Hud(window, scene);

        zone = new Zone(scene, this, hud);

        avatar = new Avatar(scene, zone);
        zone.setAvatar(avatar);

        camera = new GameCamera(window, avatar, zone);

        hud.camera = camera;
        zone.camera = camera;

        // The target is actually the main camera since it doesn't track during jumps, etc.
        map = new MiniMap(mapScene, avatar, camera, hud, window);
    }

    public void initialize() throws Exception {

        initializeTileShader();

        zone.requestZone("temple", "", 0.0f);

        // Setup Lights
        setupLights();
    }

    private void startZoneFadeOut() {

        float targetHeading = zone.getRequestedTargetHeading();

        // New zone on the same plane is just a snap to black then fade in.
        if (targetHeading == 0.0f) {
            startZone();
            return;
        }
        
        // New zone on the adjacent plane is a fade out then heading transition.
        zone.transition.startFadeOutTransition();

        hud.startFadeOut();
    }

    private void startZone() {

        float oldZoneHeading = zone.getMapHeading();

        zone.loadRequestedZone(map);

        float newZoneHeading = zone.getMapHeading(); 

        if (zone.enteredByDoor()) {

            camera.setHeading(0.0f);
            if (newZoneHeading > oldZoneHeading) {
                zone.transition.startHeadingTransition();
                camera.setHeading(90.0f);

                camera.forceTargetOffset();
                zone.hideUnenteredDoors();
            }

            else if (newZoneHeading < oldZoneHeading) {
                zone.transition.startHeadingTransition();
                camera.setHeading(-90.0f);

                camera.forceTargetOffset();
                zone.hideUnenteredDoors();
            }
        }
        
        // Go to start AFTER the forced target offset is recorded.
        avatar.goToStart();

        if (!zone.transition.headingTransition()) {
            hud.setFadeOut();
            hud.startFadeIn();
    
            zone.transition.startPanTransition();
        }

        count += 1;

        //mapScene.addEntity(zone.zoneRoot);
        map.addZone(zone.getName(), zone.getMapOffset(), zone.getMapHeading(), zone.getAvatarBounds());
        map.enterZone(zone.getName());
    }

    private void startZoneFadeIn() {

        hud.startFadeIn();
        zone.transition.endTransition();

        camera.releaseTargetOffset();

        zone.unhideUnenteredDoors();
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
        sceneLighting.setAmbientLight(new Vector3f(0.5f, 0.5f, 0.5f));

        // Directional Light
        float lightIntensity = 1.0f;//0.75f;
        Vector3f lightDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.flags.viewSpace = true;
        sceneLighting.addDirectionalLight(directionalLight);
        
        directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.flags.viewSpace = true;
        sceneLighting.addDirectionalLight(directionalLight);

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

        camera.input(input);

        scene.input(input);
    }

    public void update(float interval) {

        // Fade out.
        // Record position of avatar
        // Load new scene
        // Record starting position
        // Move & rotate to match recorded position
        // Pan & rotate to starting position


        // Check for a zone request.
        if (!zone.getRequestedZone().isEmpty()) {
            
            if (zone.transition.transitionActive()) {

                if (zone.transition.getTransitionPercent() == 1.0f) {

                    if (zone.transition.fadeOutTransition()) {
                        startZone();
                        
                        //startZoneHeadingOutTransition();

                    }// else if (zone.transition.headingOutTransition()) {
                        //startZone();
                    //}
                }

            } else {
                // Start transition out?
                if (!zone.transition.headingTransition() && !zone.transition.headingOutTransition()) {
                    startZoneFadeOut();
                }
                //startZone();
            }
        } else {

            // New zone is loaded, working through the transition.
            if (zone.transition.transitionActive()) {

                if (zone.transition.getTransitionPercent() == 1.0f) {
             
                    if (zone.transition.headingTransition()) {

                        startZoneFadeIn();
                    }
                }
            }
        }

        // Update an active transition
//        if (zone.transition.headingOutT)

        zone.update(interval);

        hud.update(interval);

        map.update(interval);

        if (accumulator >= 1.0f) {

            if (fpsSamples > 0) {
                fps = fpsTotal / fpsSamples;
            }

            accumulator = 0.0f;
            fpsTotal = 0.0f;
            fpsSamples = 0;
        }

        float avatarX = avatar.getPosition().x;
        float avatarY = avatar.getPosition().y;
        float avatarZ = avatar.getPosition().z;

        String fpsString = String.format("%.2f [%.2f, %.2f, %.2f]", fps, avatarX, avatarY, avatarZ);
        hud.setStatusText(fpsString);

        float fps = 1.0f / interval;
        accumulator += interval;
        fpsTotal += fps;
        fpsSamples++;

        //avatar.takePendingItem();
        scene.update(interval);

        // Update the camera last so that the targets transform is up to date and already simulated.
        camera.update(interval);
        //  map.getCamera().update(interval);

        /*
        // Lights are old skool non-entities. Manually transform this one by the camera view matrix for now.
        Vector3f dir3 = directionalLight.getDirection();
        dir3.set(0.0f, 0.0f, 1.0f);

        Matrix4f viewMat = new Matrix4f(camera.getViewMatrix());
        viewMat.transformDirection(dir3);
       */
        mapScene.update(interval);
    }

    public void render(float interval) {

        // Render
        sceneRenderer.render(camera, scene, true);
        sceneRenderer.render(map.getCamera(), map.getScene(), false);
        
    }

    public void LoadSRequestEvent() {

    }

    public Entity preLoadEntityEvent(String name, Map<String, String>properties) {

        String type = properties.get("p_type");
        if (type != null) {

            if (type.equals("zone_metadata") || type.equals("zone metadata")) {

                zone.setMetadata(properties);
                return null;
            }

            else if (type.equals("door")) {
                return zone.loadDoor(properties, false, false);
            }

            else if (type.equals("front_door")) {
                return zone.loadDoor(properties, true, false);
            }

            else if (type.equals("exit")) {
                return zone.loadDoor(properties, false, true);
            }

            else if (type.equals("front_exit")) {
                return zone.loadDoor(properties, true, true);
            }

            else if (type.equals("ladder")) {
                return zone.loadLadder(properties);
            }

            else if (type.equals("npc")) {
                return zone.loadNpc(name, properties, false);
            }

            else if (type.equals("item")) {
                return zone.loadNpc(name, properties, true);
            }

            else if (type.equals("up_trigger")) {
                return zone.loadTrigger(properties, TriggerType.UP);
            }            

            else if (type.equals("down_trigger")) {
                return zone.loadTrigger(properties, TriggerType.DOWN);
            }            

            else if (type.equals("touch_trigger")) {
                return zone.loadTrigger(properties, TriggerType.TOUCH);
            }            

        } else {

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

        String type = properties.get("p_type");
        if (type != null) {

            if (type.equals("avatar_start")) {
                zone.setAvatarStart(entity.getPosition());
                return;           
            }
        }

        Mesh mesh = entity.getMesh();
        if (mesh != null) {
            Material material = mesh.getMaterial();
            if (material != null) {
                material.setShader(tileShader);
            }
        }

        String collision = properties.get("p_collision");
        if (collision != null) {
            entity.flags.collidable = true;

            if (collision.equals("platform")) {
                entity.flags.platform_collision = true;
            } else if (collision.equals("box")) {
                entity.flags.box_collision = true;
            }
        }

        zone.addEntity(entity);
    }
}