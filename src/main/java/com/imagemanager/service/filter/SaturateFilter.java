package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Saturation is increased by scaling the difference between each color component and the luminance value. A factor greater than 1 increases saturation, while a factor less than 1 decreases it.
 */
public class SaturateFilter extends AbstractImageFilter {
    /**
     * The saturation adjustment factor.
     * Values greater than 1 increase saturation, values less than 1 decrease it.
     */
    private double factor = 1.5;

    /**
     * Creates a new Saturate filter with default factor of 1.5.
     */
    public SaturateFilter() {
        super("Saturate");
    }

    /**
     * Applies the saturation adjustment to the input image.
     * For each pixel, the color components are adjusted by scaling their difference from the luminance value by the saturation factor.
     * The luminance is calculated using the standard RGB to grayscale conversion weights (0.2126, 0.7152, 0.0722).
     * @param image The source image to adjust saturation
     * @return A new image with adjusted saturation
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

                // Calculate luminance using standard RGB weights
                double luminance = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                
                // Adjust each color component by scaling its difference from luminance
                double r = luminance + (color.getRed() - luminance) * factor;
                double g = luminance + (color.getGreen() - luminance) * factor;
                double b = luminance + (color.getBlue() - luminance) * factor;

                // Clamp the color values to valid range [0,1]
                r = Math.min(Math.max(r, 0.0), 1.0);
                g = Math.min(Math.max(g, 0.0), 1.0);
                b = Math.min(Math.max(b, 0.0), 1.0);

                // Set the pixel to the adjusted color
                Color saturatedColor = new Color(r, g, b, color.getOpacity());
                pixelWriter.setColor(x, y, saturatedColor);
            }
        }

        return writableImage;
    }
}
