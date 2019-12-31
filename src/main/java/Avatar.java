import org.joml.Vector3f;

import org.engine.Entity;
import org.engine.renderer.Camera;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.renderer.Window;
import org.engine.input.Mouse;
import org.engine.resources.StaticMeshLoader;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

import org.engine.core.Math;

public class Avatar extends Entity {

//    private Entity entity;

    private Vector3f moveDir;

    private float speed = 3.5f;

    public Avatar() {

        moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public void initialize() throws Exception {

        // Avatar placeholder.
        Mesh[] avatarMesh = StaticMeshLoader.load("src/main/resources/models/human01.fbx", "src/main/resources/models/");
        Texture texture = avatarMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        avatarMesh[0].setMaterial(material);

        setMesh(avatarMesh);
    }

    public void input(Window window, Mouse mouse) {

        moveDir.set(0, 0, 0);

        if ( window.isKeyPressed(GLFW_KEY_W) ) {
            moveDir.z = -1;
        }

        if ( window.isKeyPressed(GLFW_KEY_S) ) {
            moveDir.z = 1;
        }

        if (window.isKeyPressed(GLFW_KEY_A)) {
            moveDir.x = -1;
        }

        if (window.isKeyPressed(GLFW_KEY_D)) {
            moveDir.x = 1;
        }

        if (moveDir.length() > 0.0f) {
            moveDir.normalize();
        }
    }

    public void update(float interval, Mouse mouse, Camera camera) {

        float offsetX = moveDir.x * speed * interval;
        float offsetY = moveDir.y * speed * interval;
        float offsetZ = moveDir.z * speed * interval;

        Vector3f cameraRotation = camera.getRotation();

        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(cameraRotation.y) * -1.0f * offsetZ;
            position.z += (float)Math.cos(cameraRotation.y) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(cameraRotation.y - Math.toRadians(90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(cameraRotation.y - Math.toRadians(90)) * offsetX;
        }
        position.y += offsetY;




  //      if (moveDir.length() > 0.0f) {

            moveDir.rotateY(-cameraRotation.y);


            Vector3f forward = new Vector3f();
            forward.set(Math.forward);

            forward.rotateY(-rotation.y);

            float angY = -forward.angleSigned(moveDir, Math.up);

            rotation.y = rotation.y + angY;

    //    }



        //entity.setRotation(rotation);
    }

    public Entity getEntity() {

        return this;//entity;
    }
}
