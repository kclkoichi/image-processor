package com.kcl.osc.imageprocessor;

import javafx.scene.paint.Color;

/**
 * Applies the greyscale filter.
 */
public class GreyscaleWorker extends Worker{

    public GreyscaleWorker(Color[][] baseImage, int start, int end, Color[][] result) {
        super(baseImage, start, end, result);
    }

    /**
     * Applies the greyscale filter to a part of an image,
     * from the startRow (inclusive) to the endRow (exclusive).
     * Modifies the filteredImage.
     */
    @Override
    public void run() {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < (image[i].length); j++) {

                double red = image[i][j].getRed();
                double green = image[i][j].getGreen();
                double blue = image[i][j].getBlue();

                double newRGB = (red + green + blue) / 3;
                newRGB = clampRGB(newRGB);

                Color newPixel = new Color(newRGB, newRGB, newRGB, 1.0);
                filteredImage[i][j] = newPixel;
            }
        }
    }

}
