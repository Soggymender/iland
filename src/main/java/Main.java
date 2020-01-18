import org.engine.core.Timer;
import org.engine.input.Mouse;
import org.engine.renderer.Window;
import org.engine.scene.Scene;
import org.engine.scene.SceneRenderer;

public class Main {

    public static void main(String[] args) {
        try {

            // Create the "services" that we need.

            Window window = new Window("Game", 720, 480, false);

            Timer timer = new Timer();
            Mouse mouse = new Mouse();
            Scene scene = new Scene();
            SceneRenderer sceneRenderer = new SceneRenderer();

            Game game = new Game(scene);

            window.initialize();
            mouse.initialize(window);
            timer.initialize();
            sceneRenderer.initialize(window);

            game.initialize(window);

            float elapsedTime;

            while (!window.windowShouldClose()) {

                elapsedTime = timer.getElapsedTime();

                // Input
                mouse.input(window);
                game.input(window, mouse);
                scene.input(mouse);

                // Update
                game.update(elapsedTime, mouse);
                scene.update(elapsedTime);

                // Render
                game.render(window);
                sceneRenderer.render(window, scene);
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