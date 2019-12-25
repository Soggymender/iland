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

    private Transform transform;

    private Shader shader;

    public Renderer() {
        transform = new Transform();
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

        shader.createUniform("color");
        shader.createUniform("useColor");
    }

    public void shutdown() {
        if (shader != null) {
            shader.shutdown();
        }
   }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Entity[] entities) {
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

        shader.setUniform("texture_sampler", 0);

        for (Entity entity : entities) {

            Mesh mesh = entity.getMesh();

            Matrix4f modelViewMatrix = transform.getModelViewMatrix(entity, viewMatrix);

            shader.setUniform("modelViewMatrix", modelViewMatrix);

            Vector4f ambientColor = mesh.getMaterial().getAmbientColour();
            Vector3f color = new Vector3f(ambientColor.x, ambientColor.y, ambientColor.z);

            shader.setUniform("color", color);
            shader.setUniform("useColor", mesh.getMaterial().isTextured() ? 0 : 1);

            mesh.render();
        }

        shader.unbind();
    }
}