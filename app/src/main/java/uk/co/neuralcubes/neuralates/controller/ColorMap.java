package uk.co.neuralcubes.neuralates.controller;

import android.support.annotation.NonNull;

/**
 * Created by javi on 19/04/16.
 */
public class ColorMap {

    /*
     * I got these values from http://colorbrewer2.org/
     * and I assume that the gradients are linear, although they
     * are not strictly linear
     */
    public static final ColorMap GREENISH = new ColorMap(new int[]{247,252,245}, new int[]{0,68,27});
    public static final ColorMap REDISH = new ColorMap(new int[]{255,245,240}, new int[]{103,0,13});
    public static final ColorMap BLUEISH = new ColorMap(new int[]{247,251,255}, new int[]{8,48,107});

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
    public int[] inverseMap(double value) {
        return this.map(1 - value);
    }

    private int normalise(int max,int min, double value) {
        // project onto the straight line
        return (int)(value*(max-min)+min);
    }

    public static ColorMap next(@NonNull ColorMap map){
        if(map == GREENISH){
            return REDISH;
        }
        if (map== REDISH){
            return BLUEISH;
        }
        return GREENISH;
    }

}
