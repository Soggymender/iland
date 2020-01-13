package org.engine.core;

public class Rect {

    public float xMin = 0;
    public float yMin = 0;
    public float yMax = 0;
    public float xMax = 0;

    public Rect() {

    }

    public Rect(float x, float y, float width, float height) {
        this(x, y, width, height, false);
    }

    public Rect(float xMin, float yMin, float xMax, float yMax, boolean minMax) {

        if (minMax) {
            setMinMax(xMin, yMin, xMax, yMax);
        } else {
            set(xMin, yMin, xMax, yMax);
        }
    }

    public Rect copy() {
        Rect copy = new Rect();
        copy.set(this);
        return copy;
    }

    public boolean equal(Rect rect) {
        return this.xMin == rect.xMin &&
                this.yMin == rect.yMin &&
                this.xMax == rect.xMax &&
                this.yMax == rect.yMax;
    }

    public void set(Rect rect) {
        xMin = rect.xMin;
        yMin = rect.yMin;
        xMax = rect.xMax;
        yMax = rect.yMax;
    }

    public void set(float x, float y, float width, float height) {
        xMin = x;
        yMin = y;
        xMax = x + width;// - 1;
        yMax = y + height;// - 1;
    }

    public void setMinMax(float xMin, float yMin, float xMax, float yMax) {

        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public void scale(float scale) {
        xMin *= scale;
        yMin *= scale;
        xMax *= scale;
        yMax *= scale;
    }

    public float getWidth() {
        return xMax - xMin;// + 1;
    }

    public float getHeight() {
        return yMax - yMin;// + 1;
    }
}
