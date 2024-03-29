package org.tiland;

import org.engine.core.BoundingBox;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.engine.input.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

public class Avatar extends Sprite {

    private Zone zone = null;

    public boolean crouch = false;
    public boolean enter = false;

    private Vector3f crouchScale = new Vector3f(1.0f, 0.5f, 1.0f);
    private Vector3f standScale = new Vector3f(1.0f, 1.0f, 1.0f);

    public Avatar(Scene scene, Zone zone) throws Exception {

        super(scene);

        this.zone = zone;

        moveVec = new Vector2f();

        initialize(scene);
    }

    public void initialize(Scene scene) throws Exception {

        setPosition(-0.1f, 1.0f, 0.01f);

        // Avatar placeholder.
        Mesh[] avatarMesh = SceneLoader.loadMesh("src/main/resources/tiland/models/avatar.fbx", "src/main/resources/tiland/textures/");
        Texture texture = avatarMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        avatarMesh[0].setMaterial(material);

        setMeshes(avatarMesh);

        scene.addEntity(this);
    }

    public void goToStart() {

        Vector3f avatarStart = new Vector3f(zone.avatarStart);
        avatarStart.z = getPosition().z;
        
        setPosition(avatarStart);
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

        if (keyboard.keyJustDown(GLFW_KEY_W)) {
            enter = true;
        }

        if (moveVec.length() > 0.0f) {
            moveVec.normalize();
        }
    }

    @Override
    public void update(float interval) {

        if (enter) {

            enter = false;

            if (zone.enterDoor(this)) {
                return;
            }
        }

        if (jump && crouch) {

            // Crouch fall through floor, but only if its a platform, not a box.
            if (support != null) {
                if (support.flags.platform_collision) { 

                    float yVel = getVelocity().y;
                    if (java.lang.Math.abs(yVel) < 0.1f) {
                        // Nudge the position down below the standing surface.
                        position.y -= 0.1f;
                    }
                }
            }

            jump = false;
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

        BoundingBox bounds = zone.getAvatarBounds();

        // TODO: There's a bug here because frameVelocity will show a larger value than what was effectively applied.
        // But it should only matter if a collision happens that needs to be resolved while trying to pass the boundary.
        if (position.x + bBox.min.x < bounds.min.x) {
            position.x = bounds.min.x - bBox.min.x;
        }

        if (position.x +bBox.max.x > bounds.max.x) {
            position.x = bounds.max.x - bBox.max.x;
        }
    }
}