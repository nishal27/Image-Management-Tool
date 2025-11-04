package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * The contrast is increased by scaling the difference between each color component and the middle gray value (0.5). A factor greater than 1 increases contrast, while a factor less than 1 decreases it.
 */
public class ContrastFilter extends AbstractImageFilter {
    /**
     * The contrast adjustment factor.
     */
    private double factor = 1.5;

    /**
     * Creates a new Contrast filter with default factor of 1.5.
     */
    public ContrastFilter() {
        super("Contrast");
    }

    /**
     * Applies the contrast adjustment to the input image.
     * For each pixel, the color components are adjusted by scaling their difference from the middle gray value (0.5) by the contrast factor.
     * @param image The source image to adjust contrast
     * @return A new image with adjusted contrast
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

                // Adjust each color component by scaling its difference from middle gray
                double r = (color.getRed() - 0.5) * factor + 0.5;
                double g = (color.getGreen() - 0.5) * factor + 0.5;
                double b = (color.getBlue() - 0.5) * factor + 0.5;

                // Clamp the color values to valid range [0,1]
                r = Math.min(Math.max(r, 0.0), 1.0);
                g = Math.min(Math.max(g, 0.0), 1.0);
                b = Math.min(Math.max(b, 0.0), 1.0);

                // Set the pixel to the adjusted color
                Color contrastedColor = new Color(r, g, b, color.getOpacity());
                pixelWriter.setColor(x, y, contrastedColor);
            }
        }

        return writableImage;
    }
}
