package org.tiland;

import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.*;

public class Sprite extends Entity {

    private static final float ACCELERATION = 16.0f;
    private static final float MOVE_SPEED = 2.5f;
    private static final float CLIMB_SPEED = 1.5f;
    private static final float JUMP_IMPULSE = 5.0f;

    Scene scene = null;

    protected Vector2f moveVec;
    protected Vector2f oldMoveVec;

    Vector2f moveAccel;
    Vector2f moveSpeed;
    float moveDrag = 48.0f;

    protected boolean jump = false;

    float gravity = 10.0f;
    float gravityScalar = 1.0f;

    float frictionScalar = 1.0f;

    public Sprite(Scene scene) {

        this.scene = scene;

        moveVec = new Vector2f();
        oldMoveVec = new Vector2f();

        moveAccel = new Vector2f();
        moveSpeed = new Vector2f();

        initialize();
    }

    public void initialize() {

        // Setting this to dynamic allows automatic collision resolution against static entities.
        flags.dynamic = true;
        flags.collidable = true;

    }

    @Override
    public void update(float interval) {

        if (flags.dynamic) {
            moveSpeed.x = getVelocity().x;
            moveSpeed.y = getVelocity().y;

            // Apply jump.
            if (jump) {
                
                if (java.lang.Math.abs(moveSpeed.y) < 0.1f) {
                    // Jump just crushes y velocity.
                    moveSpeed.y = JUMP_IMPULSE;
                    
                }
            }

            // Apply gravity.
            moveSpeed.y -= gravity * gravityScalar * interval;
            if (moveSpeed.y < -gravity) {
                moveSpeed.y = -gravity;
            }

            // Apply ground friction.
            float moveLength = java.lang.Math.abs(moveSpeed.x);

            if (moveLength != 0.0f) {

                float newMoveLength = moveLength;

                newMoveLength -= moveDrag * frictionScalar * interval;
                if (newMoveLength < 0.0f) {
                    newMoveLength = 0.0f;
                }

                // Normalize just x component, multiply by new length.
                moveSpeed.x /= moveLength;
                moveSpeed.x *= newMoveLength;
            }

            // Apply climbing friction.
            moveLength = java.lang.Math.abs(moveSpeed.y);

            if (gravityScalar == 0.0f && moveLength != 0.0f) {

                float newMoveLength = moveLength;

                newMoveLength -= moveDrag * interval;
                if (newMoveLength < 0.0f) {
                    newMoveLength = 0.0f;
                }

                // Normalize just x component, multiply by new length.
                moveSpeed.y /= moveLength;
                moveSpeed.y *= newMoveLength;
            }

            // If changing directions horizontally, bash acceleration instantly.
            if (moveVec.x > 0.0f && oldMoveVec.x < 0.0f ||
                moveVec.x < 0.0f && oldMoveVec.x > 0.0f) {
                moveAccel.x = 0.0f;
            }

            // If changing directions vertically, bash acceleration instantly.
            if (moveVec.y > 0.0f && oldMoveVec.y < 0.0f ||
                moveVec.y < 0.0f && oldMoveVec.y > 0.0f) {
                moveAccel.y = 0.0f;
            }

            // Apply ground movement.
            if (java.lang.Math.abs(moveVec.x) > 0.0f) {
                
                moveAccel.x += ACCELERATION * interval;
                if (moveAccel.x > MOVE_SPEED) {
                    moveAccel.x = MOVE_SPEED;
                }
                moveSpeed.x = moveVec.x * moveAccel.x;//MOVE_SPEED;
            } else{
                moveAccel.x = 0.0f;
            }

            // Apply climbing movement.
            if (java.lang.Math.abs(moveVec.y) > 0.0f) {
                
                moveAccel.y += ACCELERATION * interval;
                if (moveAccel.y > CLIMB_SPEED) {
                    moveAccel.y = CLIMB_SPEED;
                }
                moveSpeed.y = moveVec.y * moveAccel.y;//MOVE_SPEED;
            } else{
                moveAccel.y = 0.0f;
            }

            Vector3f pos = getPosition();
        
            // Clip to y = 0 until collision works.
        //    if (pos.y <= -0.4f && !jump && moveSpeed.y < 0.0f) {
          //      pos.y = -0.4f;
           //     moveSpeed.y = 0.0f;
           // }

            // TODO: For now, this is just used to calculate collision resolution.
            // Later it could be used to handle movement automatically.
            setVelocity(moveSpeed.x, moveSpeed.y, 0.0f);

            jump = false;
        }

        super.update(interval);

        oldMoveVec.x = moveVec.x;
        oldMoveVec.y = moveVec.y;
    }
}