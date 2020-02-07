package org.engine.scene;

import java.util.List;
import java.util.Map;

import org.engine.renderer.*;
import org.engine.renderer.shaders.*;
import org.engine.Utilities;

import static org.lwjgl.opengl.GL11.*;

import org.engine.core.*;
import org.joml.Matrix4f;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class SceneRenderer {

    Window window = null;

    private static final float FOV = (float)java.lang.Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    public static final int MAX_POINT_LIGHTS = 5;
    public static final int MAX_SPOT_LIGHTS = 5;

    private Transform transform;

    private Shader defaultShader;
    private Shader skyboxShader;
    private Shader hudShader;

    private float specularPower;

    public SceneRenderer(Window window) throws Exception {

        this.window = window;

        transform = new Transform();
        specularPower = 10.0f;

        initializeDefaultShader();
        initializeSkyboxShader();
        initializeHudShader();
    }

    private void initializeDefaultShader() throws Exception {

        ShaderCache shaderCache = ShaderCache.getInstance();
        defaultShader = shaderCache.addShader("default");

        String vsName = Utilities.load("/shaders/default_vertex.vs");
        String fsName = Utilities.load("/shaders/default_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        defaultShader.createVertexShader(vsName);
        defaultShader.createFragmentShader(fsName);
        defaultShader.link();

        DefaultUniformManager uniformManager = new DefaultUniformManager(defaultShader);
        defaultShader.setUniformManager(uniformManager);
    }

    private void initializeSkyboxShader() throws Exception {

        ShaderCache shaderCache = ShaderCache.getInstance();
        skyboxShader = shaderCache.addShader("defaultSkybox");

        String vsName = Utilities.load("/shaders/skybox_vertex.vs");
        String fsName = Utilities.load("/shaders/skybox_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        skyboxShader.createVertexShader(vsName);
        skyboxShader.createFragmentShader(fsName);
        skyboxShader.link();

        SkyboxUniformManager uniformManager = new SkyboxUniformManager(skyboxShader);
        skyboxShader.setUniformManager(uniformManager);
    }

    private void initializeHudShader() throws Exception {
        ShaderCache shaderCache = ShaderCache.getInstance();
        hudShader = shaderCache.addShader("defaultGui");

        hudShader.createVertexShader(Utilities.load("/shaders/hud_vertex.vs"));
        hudShader.createFragmentShader(Utilities.load("/shaders/hud_fragment.fs"));
        hudShader.link();

        GuiUniformManager uniformManager = new GuiUniformManager(hudShader);
        hudShader.setUniformManager(uniformManager);
    }

    public void shutdown() {
        if (defaultShader != null) {
            defaultShader.shutdown();
        }

        if (skyboxShader != null) {
            skyboxShader.shutdown();
        }

        if (hudShader != null) {
            hudShader.shutdown();
        }
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Scene scene) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        Camera camera = scene.getCamera();

        transform.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        transform.updateOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        transform.updateViewMatrix(camera);

        Map<Shader, List<Mesh>> mapShaders = scene.getMeshShaders();
        for (Shader shader : mapShaders.keySet()) {

            // Get the meshes that use this shader.
            List<Mesh> meshList = mapShaders.get(shader);

            renderShaderMeshes(shader, scene, meshList);
        }
    }

    private void renderShaderMeshes(Shader shader, Scene scene, List<Mesh> meshList) {

        shader.bind();

        IUniformManager uniformManager = shader.getUniformManager();

        uniformManager.setShaderUniforms(transform);

        if (uniformManager.getUseSceneLighting()) {
            setLightingUniforms(shader, scene.getSceneLighting());
        }
        
        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();

        for (Mesh mesh : meshList) {

            uniformManager.setMeshUniforms(mesh, transform);

            mesh.renderList(mapMeshes.get(mesh), (Entity entity) -> {

                // TODO: Not actually sure if this needs to be a condition since each entity is checking in the
                // inner loop.
                if (entity.getVisible() && entity.getParentVisible()) {
                    uniformManager.setEntityUniforms(scene, entity, transform);
                }
            });
        }

        shader.unbind();
    }

    private void setLightingUniforms(Shader shader, SceneLighting sceneLighting) {

        // Update the view matrix.
        Matrix4f viewMatrix = transform.getViewMatrix();

        shader.setUniform("ambientLight", sceneLighting.getAmbientLight());
        shader.setUniform("specularPower", specularPower);

        // Process Point Lights
        PointLight[] pointLightList = sceneLighting.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            shader.setUniform("pointLights", currPointLight, i);
        }

        // Process Spot Ligths
        SpotLight[] spotLightList = sceneLighting.getSpotLightList();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
            Vector3f lightPos = currSpotLight.getPointLight().getPosition();

            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            shader.setUniform("spotLights", currSpotLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLighting.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shader.setUniform("directionalLight", currDirLight);
    }
}