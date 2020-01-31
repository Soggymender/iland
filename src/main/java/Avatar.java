import org.joml.Vector3f;

import org.engine.input.*;

import org.engine.renderer.Camera;
import org.engine.renderer.Color;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.engine.sketch.*;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.Terrain;

import org.engine.core.Math;

public class Avatar extends Entity {

    private Vector3f moveDir;

    private float speed = 3.5f;

    private SketchElement moveDirSketch;

    public Avatar(Scene scene) throws Exception {

        moveDir = new Vector3f(0.0f, 0.0f, 0.0f);

        moveDirSketch = new SketchElement(null);
        scene.addEntity(moveDirSketch);
    }

    public void initialize() throws Exception {

        // Avatar placeholder.
        Mesh[] avatarMesh = SceneLoader.loadMesh("src/main/resources/models/human01.fbx", "src/main/resources/models/");
        Texture texture = avatarMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        avatarMesh[0].setMaterial(material);

        setMeshes(avatarMesh);
    }

    public void input(Input input) {

        moveDir.set(0, 0, 0);

        Keyboard keyboard = input.getKeyboard();

        if (keyboard.keyDown(GLFW_KEY_W) ) {
            moveDir.z = -1;
        }

        if (keyboard.keyDown(GLFW_KEY_S) ) {
            moveDir.z = 1;
        }

        if (keyboard.keyDown(GLFW_KEY_A)) {
            moveDir.x = -1;
        }

        if (keyboard.keyDown(GLFW_KEY_D)) {
            moveDir.x = 1;
        }

        if (moveDir.length() > 0.0f) {
            moveDir.normalize();
        }
    }

    public void update(float interval, Camera camera, Terrain terrain) {

        // Move

        float offsetX = moveDir.x * speed * interval;
    //   float offsetY = moveDir.y * speed * interval;
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

        //position.y += offsetY;

  //      float oldY = position.y;


        // "gravity"
//        position.y -= 9.8f * interval;




        // Terrain collision
        if (terrain != null) {

            position.y = -500;

            float height = terrain.getHeight(position);
            if (position.y <= height) {
                //        position.y = oldY;
                position.y = height;
            }
        }



        // Turn

        moveDir.rotateY(-cameraRotation.y);


        Vector3f forward = new Vector3f();
        forward.set(Math.forward);

        forward.rotateY(-rotation.y);

        float angY = -forward.angleSigned(moveDir, Math.up);

        rotation.y = rotation.y + angY;
 
 
 
 
        moveDirSketch.clear();
        moveDirSketch.addLines(Color.BLACK, position.x, position.y, position.z, position.x + forward.x, position.y, position.z + forward.z);
    }

    public Entity getEntity() {

        return this;//entity;
    }
}
