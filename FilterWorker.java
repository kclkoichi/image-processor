package com.kcl.osc.imageprocessor;

import javafx.scene.paint.Color;

/**
 * Applies the given filter.
 */
public class FilterWorker extends Worker {

    private float[][] filter;

    public FilterWorker(Color[][] baseImage, int start, int end, Color[][] result, float[][] filter) {
        super(baseImage, start, end, result);
        this.filter = filter;
    }

    /**
     * Applies the filter to a part of an image,
     * from the startRow (inclusive) to the endRow (exclusive).
     * Modifies the filteredImage.
     */
    @Override
    public void run() {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 1; j < image[i].length -1; j++) {

                double red = 0.0;
                double green = 0.0;
                double blue = 0.0;

                for (int k = -1; k < filter.length - 1; k++) {
                    for (int l = -1; l < filter[0].length - 1; l++) {
                        red += image[i + k][j + l].getRed() * filter[1 + k][1 + l];
                        green += image[i + k][j + l].getGreen() * filter[1 + k][1 + l];
                        blue += image[i + k][j + l].getBlue() * filter[1 + k][1 + l];
                    }
                }

                red = clampRGB(red);
                green = clampRGB(green);
                blue = clampRGB(blue);
                filteredImage[i - 1][j - 1] = new Color(red,green,blue,1.0);

            }
        }
    }
}
