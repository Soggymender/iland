package org.tiland;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.scene.Entity;

public class Zone {

    Vector3f avatarStart;
    
    BoundingBox avatarBounds;
    BoundingBox cameraBounds;

    public Zone() {

        avatarStart = new Vector3f();
        avatarBounds = new BoundingBox();
        cameraBounds = new BoundingBox();
    }

    public void setAvatarStart(Vector3f avatarStart) {
        this.avatarStart = new Vector3f(avatarStart);
    }

    public void addEntity(Entity entity) {

        expandBounds(entity.getPosition(), entity.getBBox());
    }

    public void expandBounds(Vector3f pos, BoundingBox bBox) {

        if (pos.x + bBox.min.x < avatarBounds.min.x) {
            avatarBounds.min.x = pos.x + bBox.min.x;
        }

        if (pos.y + bBox.min.y < avatarBounds.min.y) {
            avatarBounds.min.y = pos.y + bBox.min.y;
        }

        if (pos.x + bBox.max.x > avatarBounds.max.x) {
            avatarBounds.max.x = pos.x + bBox.max.x;
        }

        if (pos.y + bBox.max.y > avatarBounds.max.y) {
            avatarBounds.max.y = pos.y + bBox.max.y;
        }

        cameraBounds.max.x = avatarBounds.max.x;
        cameraBounds.min.x = avatarBounds.min.x;
        cameraBounds.max.y = avatarBounds.max.y;
        cameraBounds.min.y = avatarBounds.min.y;

        float fov = 60.0f; // TODO: pull this from the camera.
        float camz = 4.25f; // TODO: look this up too.

        float halfFov = (float)java.lang.Math.toRadians(fov);

        // Shift this bounds to the right  to account for FOV.
        cameraBounds.min.x += halfFov * camz;
        cameraBounds.max.x -= halfFov * camz;
    }

    public BoundingBox getAvatarBounds() {
        return avatarBounds;
    }

    public BoundingBox getCameraBounds() {
        return cameraBounds;
    }
}