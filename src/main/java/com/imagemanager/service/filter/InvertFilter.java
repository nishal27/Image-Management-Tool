package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Each color component (red, green, blue) is inverted by subtracting its value from 1.0, creating a negative-like effect. The opacity (alpha) channel is preserved.
 */
public class InvertFilter extends AbstractImageFilter {

    public InvertFilter() {
        super("Color Invert");
    }

    /**
     * Applies the color inversion to the input image.
     * For each pixel, the red, green, and blue components are inverted by subtracting their values from 1.0. This creates a negative-like effect where dark areas become light and vice versa.
     * @param image The source image to invert
     * @return A new image with inverted colors
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
                Color color = pixelReader.getColor(x, y);
                
                // Create an inverted color by subtracting each component from 1.0
                Color invertedColor = new Color(
                        1.0 - color.getRed(),
                        1.0 - color.getGreen(),
                        1.0 - color.getBlue(),
                        color.getOpacity()
                );
                pixelWriter.setColor(x, y, invertedColor);
            }
        }

        return writableImage;
    }
}