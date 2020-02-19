package org.engine.scene;

import java.util.List;
import java.util.Map;

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

        for (int i = 0; i < frameEntities.size(); i++) {

            Entity a = frameEntities.get(i);
            Vector3f aPos = a.getPosition();
            BoundingBox aBox = a.getBBox();
            

            for (int j = i + 1; j < frameEntities.size(); j++) {

                Entity b = frameEntities.get(j);
                Vector3f bPos = b.getPosition();
                BoundingBox bBox = b.getBBox();
                
                if (!a.flags.collidable ||!a.flags.visible || !b.flags.collidable || !b.flags.visible) {
                    continue;
                }

                if (!a.flags.dynamic && !b.flags.dynamic) {
                    // Nothing is dynamic, nothing to do.
                    continue;
                }

                // Colliding now.
                if ((aPos.x + aBox.min.x <= bPos.x + bBox.max.x && aPos.x + aBox.max.x >= bPos.x + bBox.min.x) &&
                    (aPos.y + aBox.min.y <= bPos.y + bBox.max.y && aPos.y + aBox.max.y >= bPos.y + bBox.min.y)) {
              
                    Vector3f aPrevPos = new Vector3f(aPos);
                    if (a.flags.dynamic) {
                        Vector3f aVel = new Vector3f(a.frameVelocity);
                        aVel.x = -aVel.x;
                        aVel.y = -aVel.y;
                        aVel.z = -aVel.z;
                        aPrevPos.add(aVel);          
                    }
                    
                    Vector3f bPrevPos = new Vector3f(bPos);
                    if (b.flags.dynamic) {
                        Vector3f bVel = new Vector3f(b.frameVelocity);
                        bVel.x = -bVel.x;
                        bVel.y = -bVel.y;
                        bVel.z = -bVel.z;
                        bPrevPos.add(bVel);          
                    }

                    if (a.flags.platform_collision || b.flags.platform_collision) {
                                    
                        // Not colliding previously.
                        if ((aPrevPos.x + aBox.min.x <= bPrevPos.x + bBox.max.x && aPrevPos.x + aBox.max.x >= bPrevPos.x + bBox.min.x) &&
                            !(aPrevPos.y + aBox.min.y < bPrevPos.y + bBox.max.y && aPrevPos.y + aBox.max.y > bPrevPos.y + bBox.min.y)) {


                            // TODO: bbox specifies 2D or 3D, and auto check 3D if applicable.
                            
                            // TODO: if both are dynamic, use some weight factor to figure out how far each resolution vector is scaled.
                            if (a.flags.dynamic) {

                                Vector3f aVel = a.frameVelocity;
                                Vector3f aRes = new Vector3f();

                                if (aVel.y < 0) {
                                    aRes.y = (bPos.y + bBox.max.y) - (aPos.y + aBox.min.y);
                                }

                                a.onCollide(b, aRes);
                            }
                        }
                    } else if (a.flags.box_collision || b.flags.box_collision) {

                          // TODO: if both are dynamic, use some weight factor to figure out how far each resolution vector is scaled.
                          if (a.flags.dynamic) {

                            boolean fromLeft  = aPrevPos.x + aBox.max.x <= bPrevPos.x + bBox.min.x;
                            boolean fromRight = aPrevPos.x + aBox.min.x >= bPrevPos.x + bBox.max.x; 
                            
                            boolean fromTop = aPrevPos.y + aBox.min.y >= bPrevPos.y + bBox.max.y;
                            boolean fromBottom = aPrevPos.y + aBox.max.y <= bPrevPos.y + bBox.min.y;

                            Vector3f aVel = a.frameVelocity;
                            Vector3f aRes = new Vector3f();

                            if (fromLeft) {
                                aRes.x = (bPos.x + bBox.min.x) - (aPos.x + aBox.max.x);
                            }

                            if (fromRight) {
                                aRes.x = (bPos.x + bBox.max.x) - (aPos.x + aBox.min.x);
                            }

                            if (fromTop) {
                                aRes.y = (bPos.y + bBox.max.y) - (aPos.y + aBox.min.y);
                            }

                            a.onCollide(b, aRes);
                        }
                    }
                }
            }
        }
    }
}