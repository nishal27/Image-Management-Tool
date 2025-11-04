package com.imagemanager.service.filter;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Abstract base class for image filters.
 * Provides common functionality for creating and manipulating images.
 */
public abstract class AbstractImageFilter implements ImageFilter {
    protected String name;  // The display name of the filter

    /**
     * Creates a new filter with the specified name.
     * @param name The display name of the filter
     */
    public AbstractImageFilter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Creates a new writable image with the same dimensions as the source image.
     * @param source The source image to get dimensions from
     * @return A new writable image with the same dimensions as the source
     */
    protected WritableImage createWritableImage(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        return new WritableImage(width, height);
    }

    /**
     * Gets the pixel reader and writer for the source and target images.
     * Used to read pixels from the source and write to the target.
     * @param source The source image to read from
     * @param target The target image to write to
     * @return An array containing the pixel reader and writer in that order
     */
    protected Object[] getReadersAndWriters(Image source, WritableImage target) {
        PixelReader pixelReader = source.getPixelReader();
        PixelWriter pixelWriter = target.getPixelWriter();
        return new Object[]{pixelReader, pixelWriter};
    }
}