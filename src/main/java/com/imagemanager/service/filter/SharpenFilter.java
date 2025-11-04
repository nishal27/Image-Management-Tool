package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Uses a 3x3 convolution kernel to enhance edges and details.
 * The kernel emphasizes the center pixel while reducing the influence of surrounding pixels, creating a sharper appearance.
 */
public class SharpenFilter extends AbstractImageFilter {

    public SharpenFilter() {
        super("Sharpen");
    }

    /**
     * The kernel values are chosen to emphasize the center pixel while reducing the influence of surrounding pixels.
     * @param image The source image to sharpen
     * @return A new sharpened image
     */
    @Override
    public Image apply(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        // Sharpening kernel with center emphasis
        double[][] kernel = {
                {0, -0.5, 0},
                {-0.5, 3, -0.5},
                {0, -0.5, 0}
        };

        // Process each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double r = 0, g = 0, b = 0, a = 0;

                // Apply the convolution kernel
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        // Ensure we don't sample outside the image boundaries
                        int sampleX = Math.min(Math.max(x + kx, 0), width - 1);
                        int sampleY = Math.min(Math.max(y + ky, 0), height - 1);

                        // Get the color of the sampled pixel
                        Color color = pixelReader.getColor(sampleX, sampleY);
                        double weight = kernel[ky+1][kx+1];

                        // Apply the kernel weight to each color component
                        r += color.getRed() * weight;
                        g += color.getGreen() * weight;
                        b += color.getBlue() * weight;
                        a += color.getOpacity();
                    }
                }

                // Clamp the color values to valid range [0,1]
                r = Math.min(Math.max(r, 0.0), 1.0);
                g = Math.min(Math.max(g, 0.0), 1.0);
                b = Math.min(Math.max(b, 0.0), 1.0);
                a = Math.min(Math.max(a / 9, 0.0), 1.0);

                // Set the pixel to the sharpened color
                Color sharpenedColor = new Color(r, g, b, a);
                pixelWriter.setColor(x, y, sharpenedColor);
            }
        }

        return writableImage;
    }
}
