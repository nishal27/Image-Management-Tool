package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Filter that flips an image vertically.
 * Each pixel from the top of the image is moved to the bottom, and vice versa, creating a mirror effect along the horizontal axis.
 */
public class FlipFilter extends AbstractImageFilter {

    /**
     * Creates a new Flip filter.
     */
    public FlipFilter() {
        super("Flip Image");
    }

    /**
     * Applies the vertical flip to the input image.
     * For each pixel at position (x,y), it is moved to position (x, height-1-y), effectively flipping the image upside down.
     * @param image The source image to flip
     * @return A new vertically flipped image
     */
    @Override
    public Image apply(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        // Process each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Read color from the mirrored y position
                Color color = pixelReader.getColor(x, height - 1 - y);
                // Write to the original position
                pixelWriter.setColor(x, y, color);
            }
        }

        return writableImage;
    }
}