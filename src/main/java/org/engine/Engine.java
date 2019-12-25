package org.engine;

import org.engine.core.Timer;
import org.engine.input.*;
import org.engine.renderer.Window;

public class Engine {//} implements Runnable {

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;

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

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;
        while (running && !window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if (!window.isvSync()) {
                sync();
            }
        }
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
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