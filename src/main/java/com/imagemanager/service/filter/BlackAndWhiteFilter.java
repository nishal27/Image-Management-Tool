package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Filter that converts an image to grayscale using the luminance method.
 * Uses the standard luminance weights for RGB components -
 * Red: 0.2126
 * Green: 0.7152
 * Blue: 0.0722
 */
public class BlackAndWhiteFilter extends AbstractImageFilter {

    public BlackAndWhiteFilter() {
        super("Black and White");
    }

    /**
     * Applies the grayscale conversion to the input image.
     * @param image The source image to convert
     * @return A new grayscale image
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
                double gray = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                Color grayColor = new Color(gray, gray, gray, color.getOpacity());
                pixelWriter.setColor(x, y, grayColor);
            }
        }

        return writableImage;
    }
}
