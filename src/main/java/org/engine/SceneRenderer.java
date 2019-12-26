package org.engine;

import org.engine.renderer.*;
import org.engine.resources.Resource;

import static org.lwjgl.opengl.GL11.*;

import org.engine.core.*;
import org.joml.Matrix4f;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class SceneRenderer {

    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    private Transform transform;

    private Shader defaultShader;
    private Shader skyboxShader;

    private float specularPower;

    public SceneRenderer() {
        transform = new Transform();
        specularPower = 10.0f;
    }

    private void initializeDefaultShader() throws Exception {

        defaultShader = new Shader();

        String vsName = Resource.load("/Shaders/default_vertex.vs");
        String fsName = Resource.load("/Shaders/default_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        defaultShader.createVertexShader(vsName);
        defaultShader.createFragmentShader(fsName);
        defaultShader.link();

        defaultShader.createUniform("projectionMatrix");
        defaultShader.createUniform("modelViewMatrix");
        defaultShader.createUniform("texture_sampler");

        defaultShader.createMaterialUniform("material");

        defaultShader.createUniform("specularPower");
        defaultShader.createUniform("ambientLight");

        defaultShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        defaultShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        defaultShader.createDirectionalLightUniform("directionalLight");
    }

    private void initializeSkyboxShader() throws Exception {

        skyboxShader = new Shader();

        String vsName = Resource.load("/Shaders/skybox_vertex.vs");
        String fsName = Resource.load("/Shaders/skybox_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        skyboxShader.createVertexShader(vsName);
        skyboxShader.createFragmentShader(fsName);
        skyboxShader.link();

        // Create uniforms for projection matrix
        skyboxShader.createUniform("projectionMatrix");
        skyboxShader.createUniform("modelViewMatrix");
        skyboxShader.createUniform("texture_sampler");
        skyboxShader.createUniform("ambientLight");
    }


    public void initialize(Window window) throws Exception {

        initializeDefaultShader();
        initializeSkyboxShader();
    }

    public void shutdown() {
        if (defaultShader != null) {
            defaultShader.shutdown();
        }

        if (skyboxShader != null) {
            skyboxShader.shutdown();
        }
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Scene scene) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        renderScene(window, camera, scene);
        renderSkybox(window, camera, scene);
    }

    private void renderScene(Window window, Camera camera, Scene scene) {

        defaultShader.bind();

        // Update the projection matrix.
        Matrix4f projectionMatrix = transform.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        defaultShader.setUniform("projectionMatrix", projectionMatrix);

        // Update the view matrix.
        Matrix4f viewMatrix = transform.getViewMatrix(camera);

        renderLights(viewMatrix, scene.getSceneLighting());

        defaultShader.setUniform("texture_sampler", 0);

        for (Entity entity : scene.getEntities()) {

            Mesh mesh = entity.getMesh();

            Matrix4f modelViewMatrix = transform.getModelViewMatrix(entity, viewMatrix);

            defaultShader.setUniform("modelViewMatrix", modelViewMatrix);
            defaultShader.setUniform("material", mesh.getMaterial());

            mesh.render();
        }

        defaultShader.unbind();
    }

    private void renderSkybox(Window window, Camera camera, Scene scene) {

        Skybox skybox = scene.getSkybox();
        if (skybox == null) {
            return;
        }

        skyboxShader.bind();

        skyboxShader.setUniform("texture_sampler", 0);

        // Update projection Matrix
        Matrix4f projectionMatrix = transform.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        skyboxShader.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transform.getViewMatrix(camera);
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        Matrix4f modelViewMatrix = transform.getModelViewMatrix(skybox, viewMatrix);
        skyboxShader.setUniform("modelViewMatrix", modelViewMatrix);
        skyboxShader.setUniform("ambientLight", scene.getSceneLighting().getAmbientLight());

        scene.getSkybox().getMesh().render();

        skyboxShader.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, SceneLighting sceneLighting) {

        defaultShader.setUniform("ambientLight", sceneLighting.getAmbientLight());
        defaultShader.setUniform("specularPower", specularPower);

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
            defaultShader.setUniform("pointLights", currPointLight, i);
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

            defaultShader.setUniform("spotLights", currSpotLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLighting.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        defaultShader.setUniform("directionalLight", currDirLight);

    }
}