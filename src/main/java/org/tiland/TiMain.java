package org.tiland;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Timer;
import org.engine.input.*;
import org.engine.renderer.Window;
import org.engine.scene.Scene;
import org.engine.scene.ScenePhysics;
import org.engine.scene.SceneRenderer;
import org.engine.sketch.Sketch;

public class TiMain {

    public static void main(String[] args) {
        try {

            // Create the "services" that we need.

            Window window = new Window("Game", 1280, 720, true);

            // Create the input devices.
            Mouse mouse = new Mouse(window);
            mouse.showCursor(true);

            Keyboard keyboard = new Keyboard(window);

            Input input = new Input(mouse, keyboard);

            Scene scene = new Scene();
            ScenePhysics  scenePhysics  = new ScenePhysics();
            SceneRenderer sceneRenderer = new SceneRenderer(window);

            Scene mapScene = new Scene();

            new Sketch();

            Game game = new Game(window, scene, mapScene, sceneRenderer);

            game.initialize();

            Timer timer = new Timer();
            
            float elapsedTime;

            while (!window.windowShouldClose()) {

                elapsedTime = timer.getElapsedTime();

                // Input
                input.input();

                if (input.getKeyboard().keyJustDown(GLFW_KEY_ESCAPE)) {
                    glfwSetWindowShouldClose(window.getWindowHandle(), true);
                }

                game.input(input);

                // Update
                game.update(elapsedTime);                

                // Physics
                scenePhysics.update(scene, elapsedTime);

                game.render(elapsedTime);

                window.update();
            }

            game.shutdown();

            sceneRenderer.shutdown();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}