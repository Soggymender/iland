package org.engine.scene;

import java.util.List;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;

public class ScenePhysics {

    public ScenePhysics() {

    }

    public void shutdown() {

    }

    public void update(Scene scene, float interval) {

        // Get the entities that were updated this frame.
        List<Entity> frameEntities = scene.getFrameEntities();

        Vector3f aNewPos = new Vector3f();
        Vector3f bNewPos = new Vector3f();

        for (int i = 0; i < frameEntities.size(); i++) {

            Entity a = frameEntities.get(i);
            Vector3f aPos = a.getPosition();
            BoundingBox aBox = a.getBBox();
            float aResScalar = 1.0f;
            
            if (!a.flags.collidable || !a.flags.visible) {
                continue;
            }

            if (!a.flags.dynamic) {
                aResScalar = 0.0f;
            }

            for (int j = i + 1; j < frameEntities.size(); j++) {

                Entity b = frameEntities.get(j);
                Vector3f bPos = b.getPosition();
                BoundingBox bBox = b.getBBox();
                float bResScalar = 1.0f;
                
                if (!b.flags.collidable || !b.flags.visible) {
                    continue;
                }

                if (!a.flags.dynamic && !b.flags.dynamic) {
                    // Nothing is dynamic, nothing to do.
                    continue;
                }

                if (!b.flags.dynamic) {
                    bResScalar = 0.0f;
                }

                if (b.flags.dynamic && b.flags.dynamic) {
                    aResScalar = 0.5f;
                    bResScalar = 0.5f;
                }

                aNewPos.x = aPos.x + a.frameVelocity.x;
                aNewPos.y = aPos.y + a.frameVelocity.y;
                aNewPos.z = aPos.z + a.frameVelocity.z;

                bNewPos.x = bPos.x + b.frameVelocity.x;
                bNewPos.y = bPos.y + b.frameVelocity.y;
                bNewPos.z = bPos.z + b.frameVelocity.z;

                // Colliding next.
                if ((aNewPos.x + aBox.min.x <= bPos.x + bBox.max.x && aNewPos.x + aBox.max.x >= bPos.x + bBox.min.x) &&
                    (aNewPos.y + aBox.min.y <= bPos.y + bBox.max.y && aNewPos.y + aBox.max.y >= bPos.y + bBox.min.y)) {
              
                    // TODO: if both are dynamic, use some weight factor to figure out how far each resolution vector is scaled.
                    if (a.flags.dynamic) {

                        boolean boxCollision = a.flags.box_collision || b.flags.box_collision;

                        boolean fromLeft  = aPos.x + aBox.max.x <= bPos.x + bBox.min.x;
                        boolean fromRight = aPos.x + aBox.min.x >= bPos.x + bBox.max.x; 
                        
                        boolean fromTop = aPos.y + aBox.min.y + 0.001f >= bPos.y + bBox.max.y;
                        boolean fromBottom = aPos.y + aBox.max.y <= bPos.y + bBox.min.y;

                //        float fromTopVal = (aPos.y + aBox.min.y) - (bPos.y + bBox.max.y);

                        Vector3f aRes = new Vector3f();

                        boolean collide = false;
                        if (fromLeft && boxCollision) {
                            collide = true;
                            aRes.x = (bNewPos.x + bBox.min.x) - (aNewPos.x + aBox.max.x);
                        }

                        if (fromRight && boxCollision) {
                            collide = true;
                            aRes.x = (bNewPos.x + bBox.max.x) - (aNewPos.x + aBox.min.x);
                        }

                        if (fromBottom && boxCollision) {
                            collide = true;
                            aRes.y = (bNewPos.y + bBox.min.y) - (aNewPos.y + aBox.max.y);
                        }
                        
                        if (fromTop) {
                            collide = true;
                            aRes.y = (bNewPos.y + bBox.max.y) - (aNewPos.y + aBox.min.y);
                        }

                        if (collide) {
                            Vector3f bRes = new Vector3f(aRes);
                            a.onCollide(b, aRes.mul(aResScalar));
                            b.onCollide(a, bRes.mul(-bResScalar));
                        }
                    }
                }
            }
        }

        // Now the frame velocities have all been adjusted. Apply them to the positions.
        for (int i = 0; i < frameEntities.size(); i++) {

            Entity a = frameEntities.get(i);
            if (a.numCollisions > 0) {

                a.resolutionVec.x /= a.numCollisions;
                a.resolutionVec.y /= a.numCollisions;
                a.resolutionVec.z /= a.numCollisions;

                a.frameVelocity.add(a.resolutionVec);

                if (a.resolutionVec.x != 0) {
                    a.velocity.x = 0;
                }

                if (a.resolutionVec.y != 0) {
                    a.velocity.y = 0;
                }
            }

            a.position.add(a.frameVelocity);
        }
    }
}