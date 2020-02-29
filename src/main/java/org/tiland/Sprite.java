package org.tiland;

import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.joml.*;

import org.engine.core.Math;

public class Sprite extends Entity {

    private static final float MOVE_SPEED = 4.0f;
    private static final float JUMP_IMPULSE = 5.0f;

    Scene scene = null;

    protected Vector2f moveVec;

    Vector2f moveSpeed;
    float moveDrag = 16.0f;

    protected boolean jump = false;

    float gravity = 10.0f;

    public Sprite(Scene scene) throws Exception{

        this.scene = scene;

        moveVec = new Vector2f();
        moveSpeed = new Vector2f();

        initialize();
    }

    public void initialize() throws Exception {

        // Setting this to dynamic allows automatic collision resolution against static entities.
        flags.dynamic = true;
        flags.collidable = true;

    }

    @Override
    public void update(float interval) {

        moveSpeed.x = getVelocity().x;
        moveSpeed.y = getVelocity().y;

        // Apply jump.
        if (jump) {
            
            if (java.lang.Math.abs(moveSpeed.y) < 0.1f) {
                // Jump just crushes y velocity.
                moveSpeed.y = JUMP_IMPULSE;
                
            }
            else {
                System.out.println(moveSpeed.y);
            }
        }

        // Apply gravity.
        moveSpeed.y -= gravity * interval;
        if (moveSpeed.y < -gravity) {
            moveSpeed.y = -gravity;
        }

        // Apply ground friction.
        float moveLength = java.lang.Math.abs(moveSpeed.x);

        if (moveLength != 0.0f) {

            float newMoveLength = moveLength;

            newMoveLength -= moveDrag * interval;
            if (newMoveLength < 0.0f) {
                newMoveLength = 0.0f;
            }

            // Normalize just x component, multiply by new length.
            moveSpeed.x /= moveLength;
            moveSpeed.x *= newMoveLength;
        }

        // Apply ground movement.
        if (moveVec.length() > 0.0f) {
            moveSpeed.x = moveVec.x * MOVE_SPEED;
        }

        Vector3f pos = getPosition();
    
        // Clip to y = 0 until collision works.
        if (pos.y <= 0.0f && !jump) {
            pos.y = 0.0f;
            moveSpeed.y = 0.0f;
        }

        // TODO: For now, this is just used to calculate collision resolution.
        // Later it could be used to handle movement automatically.
        setVelocity(moveSpeed.x, moveSpeed.y, 0.0f);

        jump = false;

        super.update(interval);
    }
}