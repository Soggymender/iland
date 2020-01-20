package org.engine.scene;

import java.util.List;
import java.util.Map;

import org.engine.renderer.*;
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

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

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

        // Create uniforms for projection matrix
        skyboxShader.createUniform("projectionMatrix");
        skyboxShader.createUniform("modelViewMatrix");
        skyboxShader.createUniform("texture_sampler");
        skyboxShader.createUniform("ambientLight");
    }

    private void initializeHudShader() throws Exception {
        ShaderCache shaderCache = ShaderCache.getInstance();
        hudShader = shaderCache.addShader("defaultGui");

        hudShader.createVertexShader(Utilities.load("/shaders/hud_vertex.vs"));
        hudShader.createFragmentShader(Utilities.load("/shaders/hud_fragment.fs"));
        hudShader.link();

        // Create uniforms for Ortographic-model projection matrix and base colour
        hudShader.createUniform("projModelMatrix");
        hudShader.createUniform("color");
        hudShader.createUniform("hasTexture");
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
        transform.updateViewMatrix(camera);

        Map<Shader, List<Mesh>> mapShaders = scene.getMeshShaders();
        for (Shader shader : mapShaders.keySet()) {

            List<Mesh> meshList = mapShaders.get(shader);

            // TODO: Each shader should have a class to load entity instance data into the shader uniforms.
            // The engine should provide one class per default shader, and the user should be able to provide one per
            // custom shader. Once those exist, these three calls can be smashed into one that dynamically binds
            // the correct shader, and calls the class to load the uniforms per entity.
            if (shader == defaultShader) {
                renderScene(scene, meshList);
            } else if (shader == skyboxShader) {
                renderSkybox(scene, meshList);
            } else if (shader == hudShader) {
                renderHud(window, scene, meshList);
            }
        }
    }

    private void renderScene(Scene scene, List<Mesh> meshList) {

        defaultShader.bind();

        // Update the projection matrix.
        Matrix4f projectionMatrix = transform.getProjectionMatrix();
        defaultShader.setUniform("projectionMatrix", projectionMatrix);

        // Update the view matrix.
        Matrix4f viewMatrix = transform.getViewMatrix();

        renderLights(viewMatrix, scene.getSceneLighting());

        defaultShader.setUniform("texture_sampler", 0);

        // This iterates through the meshes rather than the entities, because batching multiple instances
        // of the same mesh side by side saves render state changes. So each mesh points at all of the entities
        // that use it.

        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();

        for (Mesh mesh : meshList) {//mapMeshes.keySet()) {

            defaultShader.setUniform("material", mesh.getMaterial());
            mesh.renderList(mapMeshes.get(mesh), (Entity entity) -> {
                Matrix4f modelViewMatrix = transform.buildModelViewMatrix(entity, viewMatrix);
                defaultShader.setUniform("modelViewMatrix", modelViewMatrix);
            });
        }

        defaultShader.unbind();
    }

    private void renderSkybox(Scene scene, List<Mesh> meshList) {

        skyboxShader.bind();
        skyboxShader.setUniform("texture_sampler", 0);

        // Update projection Matrix
        Matrix4f projectionMatrix = transform.getProjectionMatrix();
        skyboxShader.setUniform("projectionMatrix", projectionMatrix);


        //Matrix4f viewMatrix = new Matrix4f(transform.getViewMatrix());
        Matrix4f viewMatrix = new Matrix4f(transform.getViewMatrix());
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);

        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();

        for (Mesh mesh : meshList) {

            List<Entity> entityList = mapMeshes.get(mesh);

            for (Entity entity : entityList) {
                Matrix4f modelViewMatrix = transform.buildModelViewMatrix(entity, viewMatrix);
                skyboxShader.setUniform("modelViewMatrix", modelViewMatrix);
                skyboxShader.setUniform("ambientLight", scene.getSceneLighting().getAmbientLight());

                mesh.render();
            }
        }

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

    private void renderHud(Window window, Scene scene, List<Mesh> meshList) {
        hudShader.bind();

        Matrix4f ortho = transform.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);

        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();

        for (Mesh mesh : meshList) {

            List<Entity> entityList = mapMeshes.get(mesh);

            for (Entity entity : entityList) {

                // Set ortohtaphic and model matrix for this HUD item
                Matrix4f projModelMatrix = transform.buildOrthoProjectionModelMatrix(entity, ortho);
                hudShader.setUniform("projModelMatrix", projModelMatrix);

                hudShader.setUniform("color", mesh.getMaterial().getDiffuseColor());
                hudShader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

                mesh.render();
            }
        }

        hudShader.unbind();
    }
}