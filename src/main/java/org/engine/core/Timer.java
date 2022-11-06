package org.engine.core;

public class Timer {

    private double lastLoopTime;

    public Timer() {
        lastLoopTime = getTime();
        
        // Flush the timer.
        getElapsedTime();
    }

    public double getTime() {
        return System.nanoTime() / 1000_000_000.0;
    }

    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }

    public double getLastLoopTime() {
        return lastLoopTime;
    }

    public void flush() {
        getElapsedTime();
    }
}