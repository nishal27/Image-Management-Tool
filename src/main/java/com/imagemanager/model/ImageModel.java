package com.imagemanager.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Model class representing an image in the application.
 * Handles image loading, property extraction, and provides access to different image versions (original, display, and thumbnail).
 */
public class ImageModel {
    private File originalFile;
    private Image originalImage;
    private Image displayImage;
    private Image thumbnailImage;
    private Map<String, String> properties = new HashMap<>();
    private StringProperty statusProperty = new SimpleStringProperty();

    /**
     * Creates a new ImageModel instance and loads the image from the specified file.
     * Initializes the original, display, and thumbnail versions of the image.
     * @param file The image file to load
     */
    public ImageModel(File file) {
        this.originalFile = file;
        loadImage();
    }

    /**
     * Loads the image from the file and initializes all image versions.
     * Extracts image properties and metadata.
     * Sets appropriate status messages during the loading process.
     */
    private void loadImage() {
        try {
            originalImage = new Image(originalFile.toURI().toString());

            thumbnailImage = new Image(originalFile.toURI().toString(), 100, 100, true, true);

            displayImage = originalImage;

            extractProperties();

            statusProperty.set("Image loaded: " + originalFile.getName());
        } catch (Exception e) {
            statusProperty.set("Error loading image: " + e.getMessage());
        }
    }

    /**
     * Extracts and stores various properties of the image.
     * Includes basic properties like dimensions and file size, as well as metadata if available.
     */
    private void extractProperties() {
        // Basic file properties
        properties.put("Filename", originalFile.getName());
        properties.put("Path", originalFile.getAbsolutePath());
        properties.put("Size", formatFileSize(originalFile.length()));
        properties.put("Width", String.valueOf((int) originalImage.getWidth()) + " px");
        properties.put("Height", String.valueOf((int) originalImage.getHeight()) + " px");

        try {
            // Extract image format and metadata
            ImageInputStream iis = ImageIO.createImageInputStream(originalFile);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis, true);

                properties.put("Format", reader.getFormatName());

                // Extract metadata if available
                IIOMetadata metadata = reader.getImageMetadata(0);
                if (metadata != null) {
                    String[] metadataFormats = metadata.getMetadataFormatNames();

                    if (metadataFormats.length > 0) {
                        properties.put("Metadata Format", metadataFormats[0]);
                    }
                }

                reader.dispose();
            }

            iis.close();
        } catch (IOException e) {
            properties.put("Metadata", "Not available");
        }
    }

    /**
     * Formats a file size in bytes into a human-readable string.
     * Converts to KB or MB as appropriate.
     * @param size The file size in bytes
     * @return Formatted string with appropriate unit (B, KB, or MB)
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }

    /**
     * @return The original image file
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * @return The original loaded image
     */
    public Image getOriginalImage() {
        return originalImage;
    }

    /**
     * @return The current display version of the image (may be filtered)
     */
    public Image getDisplayImage() {
        return displayImage;
    }

    /**
     * Sets the display version of the image.
     * Used when applying filters or transformations.
     * @param displayImage The new display image
     */
    public void setDisplayImage(Image displayImage) {
        this.displayImage = displayImage;
    }

    /**
     * @return The thumbnail version of the image
     */
    public Image getThumbnailImage() {
        return thumbnailImage;
    }

    /**
     * @return Map of image properties and metadata
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @return The current status message
     */
    public String getStatusProperty() {
        return statusProperty.get();
    }

    /**
     * @return The status property for binding
     */
    public StringProperty statusPropertyProperty() {
        return statusProperty;
    }

    /**
     * Sets a new status message.
     * @param status The new status message
     */
    public void setStatusProperty(String status) {
        this.statusProperty.set(status);
    }
}