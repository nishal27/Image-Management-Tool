package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Uses a simple averaging algorithm where each pixel's color is replaced by the average color of its neighboring pixels within a specified radius.
 */
public class BlurFilter extends AbstractImageFilter {

    public BlurFilter() {
        super("Blur");
    }

    /**
     * Applies the blur effect to the input image.
     * For each pixel, calculates the average color of its 3x3 neighborhood (including the pixel itself) and sets the pixel to that average color.
     * @param image The source image to blur
     * @return A new blurred image
     */
    @Override
    public Image apply(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        int radius = 1;

        // Process each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double r = 0, g = 0, b = 0, a = 0;
                int count = 0;

                // Sample neighboring pixels within the radius
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        // Ensure we don't sample outside the image boundaries
                        int sampleX = Math.min(Math.max(x + kx, 0), width - 1);
                        int sampleY = Math.min(Math.max(y + ky, 0), height - 1);

                        // Get the color of the sampled pixel
                        Color color = pixelReader.getColor(sampleX, sampleY);
                        r += color.getRed();
                        g += color.getGreen();
                        b += color.getBlue();
                        a += color.getOpacity();
                        count++;
                    }
                }

                // Calculate the average color
                r /= count;
                g /= count;
                b /= count;
                a /= count;

                // Set the pixel to the averaged color
                Color blurredColor = new Color(r, g, b, a);
                pixelWriter.setColor(x, y, blurredColor);
            }
        }

        return writableImage;
    }
}

