package org.tiland;

import org.engine.core.Transform;
import org.engine.input.*;
import org.engine.scene.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Cursor extends Entity {

    public Cursor() {

    }

    @Override
    public void input(Input input) {

        Mouse mouse = input.getMouse();

        if (mouse.leftButtonJustPressed()) {

            Vector2f pos = mouse.getPosition();
/*            Vector3f depthPos = new Vector3f(pos.x, pos.y, 0.1f);

            Vector3f nearPos = Transform::unproject(depthPos);

            depthPos.z = 1.1f;
            Vector3f farPos = Transform::unproject(depthPos);

*/

            System.out.println("clicked " + pos.x + ", " + pos.y);
        }


    }

    @Override
    public void update(float interval) {

    }


}