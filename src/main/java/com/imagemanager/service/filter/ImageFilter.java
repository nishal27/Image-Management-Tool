package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public interface ImageFilter {
    /**
     * Applies the filter to the given image and returns the result.
     * The original image remains unchanged.
     * @param image The source image to apply the filter to
     * @return A new image with the filter applied
     */
    Image apply(Image image);

    /**
     * Returns the display name of the filter.
     * This name is shown in the UI when selecting filters.
     * @return The filter's display name
     */
    String getName();
}