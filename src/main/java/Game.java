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
    private PointLight[] pointLightList;
    private SpotLight[] spotLightList;

    private DirectionalLight directionalLight;

    private float lightAngle;

    private float spotAngle = 0;
    private float spotInc = 1;

    private static final float MOUSE_SENSITIVITY = 8.4f;
    private static final float CAMERA_POS_STEP = 1.84f;

    public Game()
    {
        renderer = new Renderer();
        camera = new Camera();
        cameraMoveDir = new Vector3f(0, 0, 0);

        lightAngle = -90;
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

        // Point Light
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        pointLightList = new PointLight[]{pointLight};

        // Spot Light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        spotLightList = new SpotLight[]{spotLight, new SpotLight(spotLight)};

        lightPosition = new Vector3f(-1, 0, 0);
        directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
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

        float lightPos = spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
        }
    }

    @Override
    public void update(float interval, Mouse mouse) {

        camera.movePosition(cameraMoveDir.x * CAMERA_POS_STEP * interval, cameraMoveDir.y * CAMERA_POS_STEP * interval, cameraMoveDir.z * CAMERA_POS_STEP * interval);

         Vector2f rotVec = mouse.getDisplayVec();
         camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY * interval, rotVec.y * MOUSE_SENSITIVITY * interval, 0);

        // Update spot light direction
        spotAngle += spotInc * 0.05f;
        if (spotAngle > 2) {
            spotInc = -1;
        } else if (spotAngle < -2) {
            spotInc = 1;
        }
        double spotAngleRad = Math.toRadians(spotAngle);
        Vector3f coneDir = spotLightList[0].getConeDirection();
        coneDir.y = (float) Math.sin(spotAngleRad);

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

        renderer.render(window, camera, entities, ambientLight, pointLightList, spotLightList, directionalLight);
    }
}