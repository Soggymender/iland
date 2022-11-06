package org.niland;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.core.Timer;
import org.engine.input.*;
import org.engine.renderer.Window;
import org.engine.scene.Scene;
import org.engine.scene.SceneRenderer;
import org.engine.sketch.Sketch;

public class Main {
    public static void main(String[] args) {
        try {

            // Create the "services" that we need.

            Window window = new Window("niland", 720, 480, false);
            window.setClearColor(1.0f, 1.0f, 1.0f, 1.0f);

            // Create the input devices.
            Mouse mouse = new Mouse(window);
            Keyboard keyboard = new Keyboard(window);

            Input input = new Input(mouse, keyboard);

            Timer timer = new Timer();
            Scene scene = new Scene();
            SceneRenderer sceneRenderer = new SceneRenderer(window);

            new Sketch();

            Game game = new Game(window, scene, sceneRenderer);

            game.initialize();

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
 

                // Render
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