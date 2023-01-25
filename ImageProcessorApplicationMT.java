package com.kcl.osc.imageprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ImageProcessorApplicationMT extends Application {
    /**
     * Change this constant to change the filtering operation. Options are
     * IDENTITY, EDGE, BLUR, SHARPEN, EMBOSS, GREY
     */
    private static final String filter = "EDGE";

    /**
     * Set this boolean to false if you do NOT wish the new images to be
     * saved after processing.
     */
    private static final boolean saveNewImages = true;

    /**
     * The number of times the filtering of all images is executed.
     * Useful to set it > 1 for performance measurement.
     */
    private final int NUMBER_OF_RUN = 5;

    /**
     * The size of the thread pool.
     */
    private final int THREAD_POOL_SIZE = 4;

    /**
     * Filters images by making use of a thread pool.
     */
    @Override
    public void start(Stage stage) throws Exception{

        // gets the images from the 'img' folder.
        ArrayList<ImageInfo> images = findImages();

        System.out.println("Working.");

        long time = 0;
        for(int k = 0; k < NUMBER_OF_RUN; k++) {

            long startTime = System.nanoTime();

            TaskPool threadPool = new TaskPool(THREAD_POOL_SIZE);
            // For each image create an ImageProcessorMT and submit it to the thread pool
            for (int i = 0; i < images.size(); i++) {
                ImageProcessorMT ip = new ImageProcessorMT(images.get(i).getImage(), filter, saveNewImages, images.get(i).getFilename() + "_filtered.png");
                threadPool.submit(ip);
            }
            // Start the thread pool
            threadPool.start();
            // Wait for all tasks in the thread pool to terminate
            threadPool.join();

            long endTime = System.nanoTime();
            time += TimeUnit.MILLISECONDS.convert((endTime-startTime), TimeUnit.NANOSECONDS);

        }
        System.out.println("Done.");

        // Calculating running time
        long averageTime = time / NUMBER_OF_RUN;
        System.out.println("Running time (average of " + NUMBER_OF_RUN + " runs): " + averageTime + "ms");

        // Kill this application
        Platform.exit();
    }

    // All code below is written by iankenny (taken from the original project).

    /**
     * This method expects all of the images that are to be processed to
     * be in a folder called img that is in the current working directory.
     * In Eclipse, for example, this means the img folder should be in the project
     * folder (alongside src and bin).
     * @return Info about the images found in the folder.
     */
    private ArrayList<ImageProcessorApplicationMT.ImageInfo> findImages() {
        ArrayList<ImageProcessorApplicationMT.ImageInfo> images = new ArrayList<ImageProcessorApplicationMT.ImageInfo>();
        Collection<File> files = listFileTree(new File("img"));
        for (File f: files) {
            if (f.getName().startsWith(".")) {
                continue;
            }
            Image img = new Image("file:" + f.getPath());
            ImageProcessorApplicationMT.ImageInfo info = new ImageProcessorApplicationMT.ImageInfo(img, f.getName());
            images.add(info);
        }
        return images;
    }

    private static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<File>();
        if (dir.listFiles() == null)
            return fileTree;
        for (File entry : dir.listFiles()) {
            if (entry.isFile())
                fileTree.add(entry) /* */;
            else
                fileTree.addAll(listFileTree(entry));
        }
        return fileTree;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Simply class to hold an Image and its filename.
     * @author iankenny
     *
     */
    private static class ImageInfo {
        private Image image;
        private String filename;

        public ImageInfo(Image image, String filename) {
            this.image = image;
            this.filename = filename;
        }

        public Image getImage() {
            return image;
        }

        public String getFilename() {
            return filename;
        }
    }
}
