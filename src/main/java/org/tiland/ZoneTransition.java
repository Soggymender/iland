package org.tiland;

public class ZoneTransition {

    boolean active = false;
    boolean headingTransition = false;
    boolean panTransition = false;
    boolean fadeOutTransition = false;
    boolean headingOutTransition = false;

    float time = 0.0f;
    float length;

    final float panLength = 0.5f;
    final float headingLength = 1.0f;
    final float headingOutLength = 1.0f;
    final float fadeOutLength = 1.0f;
    
    public boolean transitionActive() {
        return active;
    }

    public boolean headingTransition() {
        return active && headingTransition;
    }

    public boolean headingOutTransition() {
        return active && headingOutTransition;
    }

    public boolean fadeOutTransition() {
        return active && fadeOutTransition;
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


        time += interval;

        if (fadeOutTransition || headingOutTransition || headingTransition) {
            if (time > length) {
                time = length;
                // Now wait for the game to move us along.
            }
        } else {

            if (time >= length) {
                time = 0.0f;
                active = false;
                return;
            }
        }
    }

    public void endTransition() {

        time = 0.0f;
        active = false;

        clearFlags();
    }

    private void clearFlags() {

        panTransition = false;
        headingTransition = false;
        fadeOutTransition = false;
        headingOutTransition = false;
    }

    public void startFadeOutTransition() {

        clearFlags();

        active = true;
        fadeOutTransition = true;

        time = 0.0f;
        length = fadeOutLength;
    }

    public void startHeadingOutTransition() {

        clearFlags();

        active = true;
        headingOutTransition = true;

        time = 0.0f;
        length = headingOutLength;
    }

    public void startHeadingTransition() {

        clearFlags();

        active = true;
        headingTransition = true;

        time = 0.0f;
        length = headingLength;

        /*
        - Lock controls
        - Fade all but arbor and avatar
        - Rotate arbor
        - Swap zone
        - Camera shift
        - Unfade
        - Unlock controls

        */
    }

    public void startPanTransition() {
        active = true;
        panTransition = true;
        headingTransition = false;

        time = 0.0f;
        length = panLength;
    }
}
