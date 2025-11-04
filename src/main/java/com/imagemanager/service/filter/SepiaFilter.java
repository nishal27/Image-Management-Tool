package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * The sepia effect is achieved by applying specific weights to the red, green, and blue components of each pixel, creating a warm, brownish tone reminiscent of old photographs.
 */
public class SepiaFilter extends AbstractImageFilter {

    public SepiaFilter() {
        super("Sepia");
    }

    /**
     * Applies the sepia tone effect to the input image.
     * For each pixel, the color components are transformed using a weighted combination of the original RGB values. The weights are chosen to create the characteristic warm, brownish tone of sepia photographs.
     * @param image The source image to apply sepia effect
     * @return A new image with sepia tone effect
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

                // Apply sepia transformation using weighted RGB components
                double r = (color.getRed() * 0.393 + color.getGreen() * 0.769 + color.getBlue() * 0.189);
                double g = (color.getRed() * 0.349 + color.getGreen() * 0.686 + color.getBlue() * 0.168);
                double b = (color.getRed() * 0.272 + color.getGreen() * 0.534 + color.getBlue() * 0.131);

                // Clamp the color values to valid range [0,1]
                r = Math.min(r, 1.0);
                g = Math.min(g, 1.0);
                b = Math.min(b, 1.0);

                // Set the pixel to the sepia color
                Color sepiaColor = new Color(r, g, b, color.getOpacity());
                pixelWriter.setColor(x, y, sepiaColor);
            }
        }

        return writableImage;
    }
}

