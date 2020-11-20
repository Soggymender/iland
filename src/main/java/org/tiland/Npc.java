package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.joml.*;

public class Npc extends Sprite {

    private Script script = null;
    private boolean talking = false;

    public Npc(Scene scene, Vector3f position, String meshFilename, String scriptFilename) {

        super(scene);

        moveVec = new Vector2f();
        flags.dynamic = false;
        flags.collidable = false;

        initialize(scene, position, meshFilename, scriptFilename);
    }

    public void initialize(Scene scene, Vector3f position, String meshFilename, String scriptFilename) {

        setPosition(position);

        Mesh[] npcMesh;

        try {
            npcMesh = SceneLoader.loadMesh("src/main/resources/tiland/models/" + meshFilename + ".fbx",
                    "src/main/resources/tiland/textures/");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Texture texture = npcMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        npcMesh[0].setMaterial(material);

        setMeshes(npcMesh);

        if (scriptFilename != null) {
            script = new Script(scriptFilename);
        }
    }

    @Override
    public void update(float interval) {

        super.update(interval);
    }

    public void interact(Hud hud) {
        
        if (script == null) {
            return;
        }

        // Grab the current command.

        int prevCommand = script.nextCommand;

        outer:
        while (script.nextCommand < script.numCommands) {

            String command = script.commands.get(script.nextCommand);
            String[] args = command.split(":");

            script.nextCommand++;

            switch (args[0]) {

                case "talk":
                    talking = true;
                    hud.showDialog(true);
                    hud.setDialogText(args[1]);
    
                    break outer;

                case "eint":
                    endInteraction(hud, false);
                    break outer;

                case "goto":
                    script.nextCommand = Integer.parseInt(args[1]);
                    break;
                    
                default:
                    break outer;
            }
        }

        if (prevCommand == script.nextCommand) {
            // We were at the end. Close any active dialog.
            endInteraction(hud, true);            
        }
    }

    public void interruptInteraction(Hud hud) {
        
        if (talking) {
            
            hud.showDialog(false);

            script.nextCommand--;
        }
    }
    
    public void endInteraction(Hud hud, boolean reset) {
        
        talking = false;
        
        hud.showDialog(false);

        if (reset) {
            script.nextCommand = 0;
        }
    }
}
