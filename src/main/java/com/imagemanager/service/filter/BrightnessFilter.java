package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Brightness is increased by adding a constant value to each color component.
 * The factor determines how much to increase the brightness, with higher values resulting in a brighter image.
 */
public class BrightnessFilter extends AbstractImageFilter {
    /**
     * This value is added to each color component to increase brightness.
     */
    private double factor = 0.3;

    /**
     * Creates a new Brightness filter with default factor of 0.3.
     */
    public BrightnessFilter() {
        super("Brightness");
    }

    /**
     * Applies the brightness adjustment to the input image.
     * For each pixel, the brightness is increased by adding the factor to each color component (red, green, and blue).
     * The resulting values are clamped to ensure they remain in the valid range [0,1].
     * @param image The source image to adjust brightness
     * @return A new image with increased brightness
     */
    @Override
    public Image apply(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                
                Color brighterColor = new Color(
                        Math.min(color.getRed() + factor, 1.0),
                        Math.min(color.getGreen() + factor, 1.0),
                        Math.min(color.getBlue() + factor, 1.0),
                        color.getOpacity()
                );
                pixelWriter.setColor(x, y, brighterColor);
            }
        }

        return writableImage;
    }
}
