package com.kcl.osc.imageprocessor;

import javafx.scene.paint.Color;

/**
 * A Worker class that will deal with the filtering of
 * a part of the image (or its whole depending on parameters).
 */
public abstract class Worker implements Runnable{
    protected Color[][] image;
    protected Color[][] filteredImage;

    // Inclusive
    protected int startRow;
    // Exclusive
    protected int endRow;

    /**
     * Constructor.
     * @param baseImage The original image
     * @param start The row of the image to start filtering from (inclusive)
     * @param end The row of the image to end filtering (exclusive)
     * @param result The result image
     */
    public Worker(Color[][] baseImage, int start, int end, Color[][] result) {
        image = baseImage;
        startRow = start;
        endRow = end;
        filteredImage = result;
    }

    /**
     * This method ensures that the computations on color values have not
     * strayed outside of the range [0,1].
     * @param RGBValue the value to clamp.
     * @return The clamped value.
     */
    protected double clampRGB(double RGBValue) {
        if (RGBValue < 0.0) {
            return 0.0;
        } else if (RGBValue > 1.0) {
            return 1.0;
        } else {
            return RGBValue;
        }
    }

    /**
     * Applies the filter.
     * Modifies the filteredImage.
     */
    @Override
    public void run() { }
}
