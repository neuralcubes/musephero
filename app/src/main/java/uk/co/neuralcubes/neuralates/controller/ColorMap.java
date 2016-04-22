package uk.co.neuralcubes.neuralates.controller;

/**
 * Created by javi on 19/04/16.
 */
public class ColorMap {
    int mMaxRGB[];
    int mMinRGB[];

    ColorMap(int[] maxRGB, int[] minRGB) {
        mMaxRGB = maxRGB;
        mMinRGB = minRGB;
    }

    /**
     * Maps the value (0,1) to a colour
     * @param value
     * @return the rgb colour
     */
    public int[] map(double value) {
        int red = normalise(mMaxRGB[0],mMinRGB[0],value);
        int green = normalise(mMaxRGB[1],mMinRGB[1],value);
        int blue = normalise(mMaxRGB[2],mMinRGB[2],value);
        return new int[]{red,green,blue};
    }

    /**
     * Get the inverse colour of the normal map function to be used on white
     * background display
     * @param value
     * @return the rgb colour
     */
    public int[] iMap(double value) {
        return this.map(1 - value);
    }

    private int normalise(int max,int min, double value) {
        // project onto the straight line
        return (int)(value*(max-min)+min);
    }

    /*
     * I got this values from http://colorbrewer2.org/
     * and I assume that the gradients are linear, although they
     * are not strictly linear
     */
    public static ColorMap Purpleish() {
        return new ColorMap(new int[]{252,251,253}, new int[]{63,0,125});
    }

    public static ColorMap Greenish() {
        return new ColorMap(new int[]{247,252,245}, new int[]{0,68,27});
    }

    public static ColorMap Blueish() {
        return new ColorMap(new int[]{247,251,255}, new int[]{8,48,107});
    }
}
