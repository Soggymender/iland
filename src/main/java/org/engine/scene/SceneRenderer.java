package org.engine.scene;

import java.util.List;
import java.util.Map;

import org.engine.renderer.*;
import org.engine.renderer.shaders.*;
import org.engine.Utilities;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class SceneRenderer {

    Window window = null;

    public static final int MAX_POINT_LIGHTS = 5;
    public static final int MAX_SPOT_LIGHTS = 5;
    public static final int MAX_DIRECTIONAL_LIGHTS = 2;

    private Shader defaultShader;
    private Shader skyboxShader;
    private Shader hudShader;

    private float specularPower;

    public SceneRenderer(Window window) throws Exception {

        this.window = window;

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

    public void render(Camera camera, Scene scene, boolean clear) {

        Viewport viewport = camera.getViewport();

        glViewport((int)viewport.x, (int)viewport.y, (int)viewport.width, (int)viewport.height);

        if (clear) {
            clear();
        }
        
        // TODO: Maybe an odd place for this. But keep in mind that scene entity update needs this flag before it is clear.
        window.setResized(false);
 
        
        // Opaque
        Map<Shader, List<Mesh>> mapShaders = scene.getMeshShaders();
        
        // Need to make multiple passes with a z clear between each.
        // Count how many entities are on other layes to draw in future passes so we know
        // preemptively when we are done.
        int curLayer = 0;
        int numLayersRemaining = 0;
        boolean zDirty = false;

        do {

            numLayersRemaining = 0;

            if (zDirty) {
                glClear(GL_DEPTH_BUFFER_BIT);
                zDirty = false;
            }

            for (Shader shader : mapShaders.keySet()) {

                // Get the meshes that use this shader.
                List<Mesh> meshList = mapShaders.get(shader);

                numLayersRemaining += renderShaderMeshes(curLayer, camera, shader, scene, meshList, false);

            }

            zDirty = true;
            curLayer++;

        } while (numLayersRemaining > 0);
        
        glDepthMask(false);
   //     glClear(GL_DEPTH_BUFFER_BIT);

        // Transparent
        for (Shader shader : mapShaders.keySet()) {

            // Get the meshes that use this shader.
            List<Mesh> meshList = mapShaders.get(shader);

            renderShaderMeshes(0, camera, shader, scene, meshList, true);

        }
        
        glDepthMask(true);
    }

    private int renderShaderMeshes(int curLayer, Camera camera, Shader shader, Scene scene, List<Mesh> meshList, boolean transparency) {

        int numRemainingLayers = 0;

        shader.bind();

        IUniformManager uniformManager = shader.getUniformManager();

        uniformManager.setShaderUniforms(camera.getViewport());

        if (uniformManager.getUseSceneLighting()) {
            setLightingUniforms(camera, shader, scene);
        }
        
        Map<Mesh, List<Entity>> mapMeshes = scene.getEntityMeshes();

        for (Mesh mesh : meshList) {

            if (transparency != mesh.getMaterial().isTransparent()){
                continue;
            }

            uniformManager.setMeshUniforms(mesh);

            numRemainingLayers += mesh.renderList(curLayer, mapMeshes.get(mesh), (Entity entity) -> {

                // TODO: Not actually sure if this needs to be a condition since each entity is checking in the
                // inner loop.
                if (entity.getVisible() && entity.getParentVisible()) {
                    uniformManager.setEntityUniforms(camera,scene, entity);
                }
            });
        }

        shader.unbind();

        return numRemainingLayers;
    }

    private void setLightingUniforms(Camera camera, Shader shader, Scene scene) {

        SceneLighting sceneLighting = scene.getSceneLighting();
        if (sceneLighting == null) {
            return;
        }

        // Update the view matrix.
        Matrix4f viewMatrix = camera.getViewMatrix();

        if (sceneLighting.getAmbientLight() != null) {
            shader.setUniform("ambientLight", sceneLighting.getAmbientLight());
        }
        
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

        List<DirectionalLight> directionalLights = sceneLighting.getDirectionalLights();

        for (int i = 0; i < directionalLights.size(); i++) {

            DirectionalLight light = directionalLights.get(i);

            // Get a copy of the directional light object and transform its position to view coordinates
            DirectionalLight currDirLight = new DirectionalLight(light);

            if (currDirLight.flags.viewSpace) {
                Vector4f dir = new Vector4f(currDirLight.getDirection(), 0.0f);
                dir.mul(viewMatrix);
                currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
            }
                
            shader.setUniform("directionalLights", currDirLight, i);
        }
    }
}