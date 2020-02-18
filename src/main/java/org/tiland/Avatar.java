package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.engine.input.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

public class Avatar extends Sprite {

    private static final float CROUCH_JUMP_IMPULSE = -5.0f;

    public boolean crouch = false;
    private Vector3f crouchScale = new Vector3f(1.0f, 0.5f, 1.0f);
    private Vector3f standScale = new Vector3f(1.0f, 1.0f, 1.0f);

    public Avatar(Scene scene) throws Exception {

        super(scene);

        moveVec = new Vector2f();

        initialize(scene);
    }

    public void initialize(Scene scene) throws Exception {

        setPosition(-7.5f, 0, 0.01f);

        // Avatar placeholder.
        Mesh[] avatarMesh = SceneLoader.loadMesh("src/main/resources/tiland/models/avatar.fbx", "src/main/resources/tiland/textures/");
        Texture texture = avatarMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        avatarMesh[0].setMaterial(material);

        setMeshes(avatarMesh);

        scene.addEntity(this);
    }

    @Override
    public void input(Input input) {

        Keyboard keyboard = input.getKeyboard();

        moveVec.zero();

        if (keyboard.keyDown(GLFW_KEY_A)){
            moveVec.x = -1;
        } 

        if (keyboard.keyDown(GLFW_KEY_D)) {
            moveVec.x = 1.0f;
        }

        if (keyboard.keyJustDown(GLFW_KEY_SPACE)) {
            jump = true;
        }

        if (keyboard.keyDown(GLFW_KEY_S)) {
            crouch = true;
        }

        if (moveVec.length() > 0.0f) {
            moveVec.normalize();
        }
    }

    @Override
    public void update(float interval) {


        if (jump && crouch) {

            // Crouch fall through floor.
            float yVel = getVelocity().y;
            if (java.lang.Math.abs(yVel) < 0.1f) {
                // Nudge the position down below the standing surface.
                position.y -= 0.1f;
                jump = false;
            }
        }

        if (crouch) {

            // Can't move while crouching unless jumping or fall.
            float yVel = getVelocity().y;
            if (java.lang.Math.abs(yVel) < 0.1f) {
                moveVec.zero();
            }

            setScale(crouchScale);
            crouch = false;
        } else {
            setScale(standScale);
        }

        super.update(interval);
    }
}