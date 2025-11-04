package com.imagemanager.service.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating image filters.
 * Implements the Singleton pattern to ensure only one instance exists.
 * Provides methods to create filters and list available filter types.
 */
public class FilterFactory {

    private static FilterFactory instance;  // The single instance of the factory

    /**
     * Private constructor to prevent direct instantiation.
     * Use getInstance() to get the factory instance.
     */
    private FilterFactory() {
    }

    /**
     * Returns the singleton instance of the FilterFactory.
     * Creates a new instance if one doesn't exist.
     * @return The singleton instance of FilterFactory
     */
    public static synchronized FilterFactory getInstance() {
        if (instance == null) {
            instance = new FilterFactory();
        }
        return instance;
    }

    /**
     * Creates a new filter instance based on the filter name.
     * The filter name is case-insensitive.
     * @param filterName The name of the filter to create
     * @return A new instance of the requested filter
     * @throws IllegalArgumentException if the filter name is not recognized
     */
    public ImageFilter createFilter(String filterName) {
        switch (filterName.toLowerCase()) {
            case "black and white":
                return new BlackAndWhiteFilter();
            case "sepia":
                return new SepiaFilter();
            case "blur":
                return new BlurFilter();
            case "sharpen":
                return new SharpenFilter();
            case "color invert":  // Changed from "invert"
                return new InvertFilter();
            case "flip image":    // New filter
                return new FlipFilter();
            case "contrast":
                return new ContrastFilter();
            case "brightness":
                return new BrightnessFilter();
            case "saturate":
                return new SaturateFilter();
            default:
                throw new IllegalArgumentException("Unknown filter: " + filterName);
        }
    }

    /**
     * Returns a list of all available filter names.
     * These names can be used with createFilter() to create filter instances.
     * @return List of available filter names
     */
    public List<String> getAvailableFilters() {
        List<String> filters = new ArrayList<>();
        filters.add("Black and White");
        filters.add("Sepia");
        filters.add("Blur");
        filters.add("Sharpen");
        filters.add("Color Invert");  // Changed name
        filters.add("Flip Image");    // Added new filter
        filters.add("Contrast");
        filters.add("Brightness");
        filters.add("Saturate");
        return filters;
    }
}