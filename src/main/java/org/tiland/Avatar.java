package org.tiland;

import org.engine.core.BoundingBox;
import org.engine.scene.Entity;
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

    public boolean interactUp = false;
    public boolean climb = false;

    public boolean interactDown = false;
    public boolean crouch = false;
    public boolean drop = false;

    public boolean climbing = false;
    public Entity climbingEntity = null;

    public Entity interactEntity = null;

    public Inventory inventory = new Inventory();

    private Vector3f crouchScale = new Vector3f(1.0f, 0.5f, 1.0f);
    private Vector3f standScale = new Vector3f(1.0f, 1.0f, 1.0f);

    private Vector3f holdOffset = new Vector3f(-0.1f, 0.15f, 0.01f);
    private Vector3f dropOffset = new Vector3f(-0.1f, 0.0f, 0.1f);

    private Vector3f dirScale = new Vector3f(1.0f, 1.0f, 1.0f);

    public Avatar(Scene scene, Zone zone) throws Exception {

        super(scene);

        this.zone = zone;

        moveVec = new Vector2f();

        initialize(scene);
    }

    public void initialize(Scene scene) throws Exception {

        setPosition(-0.1f, 1.0f, 0.00f);

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
        
        stopClimbing();

        setPosition(avatarStart);

        // Attach to a climbable surface?
        climbingEntity = zone.climb(this);
        if (climbingEntity != null) {
            startClimbing();
        }
    }

    @Override
    public void input(Input input) {

        /*
        jump
            space
        crouch
            down
        drop
            down
        climb
            up
        enter
            up
        interact / pickvup
            up
        */

        Keyboard keyboard = input.getKeyboard();

        moveVec.zero();

        if (keyboard.keyDown(GLFW_KEY_A)){
            moveVec.x = -1;
            dirScale.x = -1.0f;
        } 

        if (keyboard.keyDown(GLFW_KEY_D)) {
            moveVec.x = 1.0f;
            dirScale.x = 1.0f;
        }

        if (keyboard.keyJustDown(GLFW_KEY_SPACE)) {
            jump = true;
            //stopClimbing();
        }

        if (/*!jump &&*/ climbingEntity != null) {

            if (keyboard.keyDown(GLFW_KEY_W)) {
                moveVec.y = 1.0f;
            }

            if (keyboard.keyDown(GLFW_KEY_S)) {
                moveVec.y = -1.0f;
            }
        } else {

            if (keyboard.keyJustDown(GLFW_KEY_W)) {
                interactUp = true;
            }

            if (keyboard.keyJustDown(GLFW_KEY_S)) {
                interactDown = true;
                crouch = true;
                drop = true;
            }

            if (keyboard.keyJustUp(GLFW_KEY_S)) {
                crouch = false;
            }

            if (keyboard.keyDown(GLFW_KEY_W)) {
                climb = true;
            }
        }

        if (moveVec.length() > 0.0f) {
            moveVec.normalize();
        }
    }

    @Override
    public void update(float interval) {

        // Continue climbing?
        if (climbingEntity != null) {

            if (!zone.entitiesOverlap(this, climbingEntity, 0.5f, 0.5f)) {
                stopClimbing();
            }
        }

        if (interactUp) {
            interactEntity = zone.interactAll(this, interactEntity, !inventory.isFull());
            if (interactEntity != null) {
                interactUp = false;

                // Don't continue interactions with items.
                // We just need to recognize them so we don't pick up in front of a door and enter with the same input.
                if (interactEntity instanceof Npc && ((Npc)interactEntity).isItem) {
                    interactEntity = null;
                }

                return;
            }
        }

        // Enter automatic or interactive doors.
        if (zone.enterDoor(this, interactUp, interactDown)) {
            interactUp = false;
            interactDown = false;
            crouch = false;
            drop = false;
            return;
        }

        interactUp = false;
        interactDown = false;

        if (interactEntity != null) {
            interactEntity = zone.validateInteraction(this, interactEntity);
        }

        if (climb) {

            float yVel = getVelocity().y;
            if (climbingEntity == null && !jump && yVel <= 0.0f) {
                climbingEntity = zone.climb(this);
                if (climbingEntity != null) {
                    startClimbing();
                    drop = false;
                }
            }

            climb = false;
        }

        if (jump && crouch) {
 
 
               // Crouch fall through floor, but only if its a platform, not a box.
                // or
                // Release climbing surface.
                if ((support != null && support.flags.platform_collision) ||
                    (climbingEntity != null)) { 

                    float yVel = getVelocity().y;
                    if (java.lang.Math.abs(yVel) < 0.1f) {
                        // Nudge the position down below the standing surface.
                        position.y -= 0.1f;

                        // Crouch jump to climb if there is a ladder below.
                        if (climbingEntity == null) {

                            climbingEntity = zone.climb(this);
                            if (climbingEntity != null) {
                                startClimbing();
                                crouch = false;
                                drop = false;
                            }
                        }
                    }
                }

                jump = false;

                if (crouch && climbingEntity != null) {
                    stopClimbing();
                    crouch = false;
                    drop = false;
                }
            

        } else if (jump && climbingEntity != null) {

            // Climbing down and jumping should just release.
            if (moveVec.y < 0.0f) {
                jump = false;
            }

            stopClimbing();
        }

        if (drop) {
            // If holding something, drop it.
            if (inventory.getHeldItem() != null) {

                // Don't allow drop on top of ladders.
                if (!(support instanceof Ladder)) {
                    drop();
                } 
            }

            drop = false;
        }

        if (crouch) {

            // Can't move while crouching unless jumping or fall.
            float yVel = getVelocity().y;
            if (java.lang.Math.abs(yVel) == 0.0f) {
                moveVec.zero();
            }
            frictionScalar = 0.2f;

            setScale(crouchScale);
            //crouch = false;
        } else {
            frictionScalar = 1.0f;
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

        updateHeldItem();
    }

    private void updateHeldItem() {

        Npc heldItem = inventory.getHeldItem();
        if (heldItem == null) {
            return;
        }

        Vector3f offset = new Vector3f(holdOffset);

        if (crouch) {

             offset.mul(crouchScale);
        }

        offset.mul(dirScale);
        offset.add(position);

        heldItem.setPosition(offset);
    }

    public void startClimbing() {
        gravityScalar = 0.0f;
        setVelocity(0.0f, 0.0f, 0.0f);
    }

    public void stopClimbing() {

        if (climbingEntity == null) {
            return;
        }

        climbingEntity = null;
        gravityScalar = 1.0f;
        getVelocity().y = 0.0f;
        moveVec.y = 0.0f;
        moveSpeed.y = 0.0f;
    }

    public void take(Npc npc) {

        if (inventory.take(npc, zone)) {
            updateHeldItem();
        }
    }

    public void drop() {

        Npc heldItem = inventory.drop(-1, true, zone);
        if (heldItem == null) {
            return;
        }

        Vector3f offset = new Vector3f(dropOffset);

        offset.mul(dirScale);
        offset.add(position);

        heldItem.setPosition(offset);
    }    
}