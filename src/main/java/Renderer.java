import org.engine.resources.Resource;

import static org.lwjgl.opengl.GL11.*;

import org.engine.core.*;
import org.engine.renderer.*;
import org.joml.Matrix4f;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer {

    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    private Transform transform;

    private Shader shader;

    private float specularPower;

    public Renderer() {
        transform = new Transform();
        specularPower = 10.0f;
    }

    public void initialize(Window window) throws Exception {

        shader = new Shader();

        String vsName = Resource.load("/Shaders/vertex.vs");
        String fsName = Resource.load("/Shaders/fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        shader.createVertexShader(vsName);
        shader.createFragmentShader(fsName);
        shader.link();

        shader.createUniform("projectionMatrix");
        shader.createUniform("modelViewMatrix");
        shader.createUniform("texture_sampler");

        shader.createMaterialUniform("material");

        shader.createUniform("specularPower");
        shader.createUniform("ambientLight");

        shader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        shader.createDirectionalLightUniform("directionalLight");
    }

    public void shutdown() {
        if (shader != null) {
            shader.shutdown();
        }
   }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Entity[] entities, Vector3f ambientLight, PointLight[] pointLightList, SpotLight[] spotLightList, DirectionalLight directionalLight) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shader.bind();

        // Update the projection matrix.
        Matrix4f projectionMatrix = transform.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shader.setUniform("projectionMatrix", projectionMatrix);

        // Update the view matrix.
        Matrix4f viewMatrix = transform.getViewMatrix(camera);

        renderLights(viewMatrix, ambientLight, pointLightList, spotLightList, directionalLight);

        shader.setUniform("texture_sampler", 0);

        for (Entity entity : entities) {

            Mesh mesh = entity.getMesh();

            Matrix4f modelViewMatrix = transform.getModelViewMatrix(entity, viewMatrix);

            shader.setUniform("modelViewMatrix", modelViewMatrix);
            shader.setUniform("material", mesh.getMaterial());

            mesh.render();
        }

        shader.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, Vector3f ambientLight, PointLight[] pointLightList, SpotLight[] spotLightList, DirectionalLight directionalLight) {

        shader.setUniform("ambientLight", ambientLight);
        shader.setUniform("specularPower", specularPower);

        // Process Point Lights
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
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shader.setUniform("directionalLight", currDirLight);

    }
}