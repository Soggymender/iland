package org.engine;

import org.engine.core.Timer;
import org.engine.input.*;
import org.engine.renderer.Window;

import static java.lang.Thread.sleep;

public class Engine {//} implements Runnable {

    private final Window window;
    private final Timer timer;

    private final IGame gameLogic;

    private final Mouse mouse;

    public Engine(String windowTitle, int width, int height, boolean vSync, IGame gameLogic) throws Exception {
        window = new Window(windowTitle, width, height, vSync);
        mouse = new Mouse();
        this.gameLogic = gameLogic;
        timer = new Timer();
    }

    //@Override
    public void run() {
        try {
            initialize();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            shutdown();
        }
    }

    protected void initialize() throws Exception {
        window.initialize();
        mouse.initialize(window);
        timer.initialize();
        gameLogic.initialize(window);
    }

    protected void shutdown() {
        gameLogic.shutdown();
    }

    protected void gameLoop() throws Exception{
        float elapsedTime;

        long targetTime = 1000L / 60;

        boolean running = true;
        while (running && !window.windowShouldClose()) {
            long startTime = (long)(timer.getTime() * 1000);

            elapsedTime = timer.getElapsedTime();

            input();

            update(elapsedTime);

            render();

            long endTime = (long)(timer.getTime() * 1000);
            //sleep(startTime + targetTime - endTime);
            //sleep(30);
       }
    }

    protected void input() {
        mouse.input(window);
        gameLogic.input(window, mouse);
    }

    protected void update(float interval) {
        gameLogic.update(interval, mouse);
    }

    protected void render() {
        gameLogic.render(window);
        window.update();
    }
}