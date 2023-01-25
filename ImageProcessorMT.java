package com.kcl.osc.imageprocessor;

import java.io.File;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageProcessorMT implements Runnable {

    private Image image;
    private String opfilename;
    private String filterType;
    private boolean save;

    // true if the filtering has ended
    private boolean ended;

    /**
     * The number of threads that will filter the image:
     * that is the number of 'slices' of the image
     */
    private static final int NUM_THREADS = 4;

    /**
     * Constructor.
     * @param image The image to process.
     * @param filter The filter to use.
     * @param save Whether to save the new image or not.
     * @param opname The output image filename
     */
    public ImageProcessorMT(Image image, String filter, boolean save, String opname) {
        this.image = image;
        this.opfilename = opname;
        this.filterType = filter;
        this.save = save;
        ended = false;
    }

    /**
     * Runs this image processor.
     */
    @Override
    public void run() {
        this.filter();
        ended = true;
    }

    /**
     * Tells if the filtering has ended or not.
     * @return true if the task has ended
     */
    public boolean hasEnded() {
        return ended;
    }

    /**
     * Creates a filtered image and saves it if needed.
     */
    private void filter(){
        Color[][] pixels = filterImage();

        if (save) {
            saveNewImage(pixels, opfilename);
        }
    }

    /**
     * This method filters an image and creates a new filtered pixel data.
     * It parallelises the filtering by ‘slicing’ the image data in NUM_THREADS slices,
     * and giving each slice to a thread to work on.
     *
     * (If NUM_THREADS exceeds the number of rows of the image,
     * only 1 thread will execute the filtering.)
     * @return the filtered image
     */
    private Color[][] filterImage() {
        // Create the threads
        Worker[] workers = new Worker[NUM_THREADS];
        Thread[] threads = new Thread[NUM_THREADS];

        Color[][] pixels = null;
        Color[][] outputPixels = null;

        if (filterType.equals("GREY")) {
            // get image WITHOUT border added
            pixels = getPixelData();
            outputPixels = new Color[pixels.length][pixels.length];

            // Create the threads
            if(pixels.length % NUM_THREADS == 0) {

                // Splits well into equal number of rows
                int numberOfRows = pixels.length / NUM_THREADS;

                for(int i = 0; i < NUM_THREADS; i++) {
                    workers[i] = new GreyscaleWorker(pixels, i*numberOfRows, i*numberOfRows + numberOfRows, outputPixels);
                    threads[i] = new Thread(workers[i]);
                }

            } else {

                // Does NOT split into equal number of rows
                int bonusRowsForLastThread = pixels.length % NUM_THREADS;
                int numberOfRows = (pixels.length - bonusRowsForLastThread) / NUM_THREADS;

                for(int i = 0; i < NUM_THREADS - 1; i++) {
                    workers[i] = new GreyscaleWorker(pixels, i*numberOfRows, i*numberOfRows + numberOfRows, outputPixels);
                    threads[i] = new Thread(workers[i]);
                }

                // Special case: the last thread takes more rows than other threads
                workers[NUM_THREADS - 1] = new GreyscaleWorker(pixels, (NUM_THREADS - 1)*numberOfRows, (NUM_THREADS - 1)*numberOfRows + numberOfRows + bonusRowsForLastThread, outputPixels);
                threads[NUM_THREADS - 1] = new Thread(workers[NUM_THREADS - 1]);

            }
        } else {
            // get image WITH border added
            pixels = getPixelDataExtended();
            outputPixels = new Color[pixels.length - 2][pixels.length - 2];

            // Create filter
            float[][] filter = createFilter(filterType);
            if(filter == null) {
                System.out.println("Invalid filterType was given: " + filterType);
                return getPixelData();
            }

            // Create the threads
            if(outputPixels.length % NUM_THREADS == 0) {

                // Splits well into equal number of rows
                int numberOfRows = outputPixels.length / NUM_THREADS;

                for(int i = 0; i < NUM_THREADS; i++) {
                    workers[i] = new FilterWorker(pixels, i*numberOfRows + 1, i*numberOfRows + numberOfRows + 1, outputPixels, filter);
                    threads[i] = new Thread(workers[i]);
                }

            } else {

                // Does NOT split into equal number of rows
                int bonusRowsForLastThread = outputPixels.length % NUM_THREADS;
                int numberOfRows = (outputPixels.length - bonusRowsForLastThread) / NUM_THREADS;

                for(int i = 0; i < NUM_THREADS - 1; i++) {
                    workers[i] = new FilterWorker(pixels, i*numberOfRows + 1, i*numberOfRows + numberOfRows + 1, outputPixels, filter);
                    threads[i] = new Thread(workers[i]);
                }

                // Special case: the last thread takes more rows than other threads
                workers[NUM_THREADS - 1] = new FilterWorker(pixels, (NUM_THREADS - 1)*numberOfRows + 1, (NUM_THREADS - 1)*numberOfRows + numberOfRows + bonusRowsForLastThread + 1, outputPixels, filter);
                threads[NUM_THREADS - 1] = new Thread(workers[NUM_THREADS - 1]);

            }
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return outputPixels;
    }

    /**
     * Creates the filter.
     * Returns null if there is no match with the given filter type.
     * @param filterType The type of filter required.
     * @return The filter.
     */
    private float[][] createFilter(String filterType) {
        filterType = filterType.toUpperCase();

        if (filterType.equals("IDENTITY")) {
            return (new float[][] {{0,0,0},{0,1,0},{0,0,0}});
        } else if (filterType.equals("BLUR")) {
            return (new float[][] {{0.0625f,0.125f,0.0625f},{0.125f,0.25f,0.125f},{0.0625f,0.125f,0.0625f}});
        } else if (filterType.equals("SHARPEN")) {
            return (new float[][] {{0,-1,0},{-1,5,-1},{0,-1,0}});
        } else if (filterType.equals("EDGE")) {
            return (new float[][] {{-1,-1,-1},{-1,8,-1},{-1,-1,-1}});
        } else if (filterType.equals("EMBOSS")) {
            return (new float[][] {{-2,-1,0},{-1,0,1},{0,1,2}});
        }
        return null;
    }

    /**
     * Saves the pixel data in the parameter as a new image file.
     * @param pixels The pixel data.
     * @param filename The output filename.
     */
    private void saveNewImage(Color[][] pixels, String filename) {
        WritableImage wimg = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());

        PixelWriter pw = wimg.getPixelWriter();
        for (int i = 0; i < wimg.getHeight(); i++) {
            for (int j = 0; j < wimg.getWidth(); j++) {
                pw.setColor(i, j, pixels[i][j]);
            }
        }

        File newFile = new File(filename);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wimg, null), "png", newFile);
        } catch (Exception s) {
        }
    }

    /**
     * Gets the pixel data from the image but does
     * NOT add a border.
     * @return The pixel data.
     */
    private Color[][] getPixelData() {
        PixelReader pr = image.getPixelReader();
        Color[][] pixels = new Color[(int) image.getWidth()][(int) image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = pr.getColor(i, j);
            }
        }

        return pixels;
    }

    /**
     * Gets the pixel data from the image but with a one-pixel border added.
     * @return The pixel data.
     */
    private Color[][] getPixelDataExtended() {
        PixelReader pr = image.getPixelReader();
        Color[][] pixels = new Color[(int) image.getWidth() + 2][(int) image.getHeight() + 2];

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels.length; j++) {
                pixels[i][j] = new Color(0.5, 0.5, 0.5, 1.0);
            }
        }

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i + 1][j + 1] = pr.getColor(i, j);
            }
        }

        return pixels;
    }
}