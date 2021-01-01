package org.tiland;

public class ZoneTransition {

    boolean active = false;
    boolean headingTransition = false;
    boolean panTransition = false;

    float time = 0.0f;
    float length;

    final float panLength = 0.5f;
    final float headingLength = 1.0f;
    
    public boolean headingTransition() {
        return active && headingTransition;
    }

    public boolean blockInput() {
        return active && (time / length <= 0.75f);
    }

    public float getTransitionPercent() {

        return time / length;
    }

    public void update(float interval) {

        if (!active)
            return;

        if (time >= length) {
            time = 0.0f;
            active = false;
            return;
        }

        time += interval;
    }

    public void startHeadingTransition() {
        active = true;
        panTransition = false;
        headingTransition = true;

        time = 0.0f;
        length = headingLength;
    }

    public void startPanTransition() {
        active = true;
        panTransition = true;
        headingTransition = false;

        time = 0.0f;
        length = panLength;
    }
}
