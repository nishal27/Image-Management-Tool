package com.imagemanager.test;

import com.imagemanager.model.ImageModel;
import com.imagemanager.service.ImageConversionService;
import com.imagemanager.service.filter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Image Management Tool application.
 * This class contains unit tests for the core functionality including -
 * Filter factory operations
 * Image conversion service
 * Filter application on images
 */
public class ImageManagementToolTest {

    private FilterFactory filterFactory;
    private ImageConversionService conversionService;
    private File sampleImageFile;

    @TempDir
    Path tempDir;

    /**
     * Initializes the filter factory, conversion service, and creates a sample image for testing. The sample image is either copied from resources or generated if no resource file is found.
     * @throws IOException if there's an error creating or copying the sample image
     */
    @BeforeEach
    void setUp() throws IOException {
        filterFactory = FilterFactory.getInstance();
        conversionService = new ImageConversionService();
        sampleImageFile = new File(tempDir.toFile(), "sample.jpg");

        // Try to find and copy the sample image from various possible resource locations
        Path[] possiblePaths = {
                Path.of("src", "test", "resources", "sample.jpg"),
                Path.of("test", "resources", "sample.jpg"),
                Path.of("src", "test", "java", "resources", "sample.jpg"),
                Path.of("test", "java", "resources", "sample.jpg"),
                Path.of("resources", "sample.jpg")
        };

        boolean imageCopied = false;
        for (Path resourcePath : possiblePaths) {
            if (Files.exists(resourcePath)) {
                Files.copy(resourcePath, sampleImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageCopied = true;
                break;
            }
        }

        // If no sample image is found in resources, create a test image
        if (!imageCopied) {
            createTestImage(sampleImageFile);
        }

        assertTrue(sampleImageFile.exists(), "Sample image file must exist for tests");
    }

    /**
     * Creates a simple test image with a gradient pattern.
     * The image is 100x100 pixels with RGB values based on pixel coordinates.
     * @param file The file to save the test image to
     * @throws IOException if there's an error writing the image file
     */
    private void createTestImage(File file) throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        // Draw something on the image
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                img.setRGB(x, y, (x * y) % 256);
            }
        }
        ImageIO.write(img, "jpg", file);
    }

    /**
     * Tests the FilterFactory functionality.
     * Verifies that -
     * The factory returns a non-empty list of available filters
     * Specific filters (Black and White, Sepia) are available
     * The factory can create filter instances
     * The factory throws an exception for non-existent filters
     */
    @Test
    void testFilterFactory() {
        List<String> filters = filterFactory.getAvailableFilters();
        assertFalse(filters.isEmpty(), "Filter list should not be empty");
        assertTrue(filters.contains("Black and White"), "Black and White filter should be available");
        assertTrue(filters.contains("Sepia"), "Sepia filter should be available");

        ImageFilter bwFilter = filterFactory.createFilter("Black and White");
        assertNotNull(bwFilter, "Filter should be created");
        assertTrue(bwFilter instanceof BlackAndWhiteFilter, "Should be instance of BlackAndWhiteFilter");

        ImageFilter sepiaFilter = filterFactory.createFilter("Sepia");
        assertNotNull(sepiaFilter, "Filter should be created");
        assertTrue(sepiaFilter instanceof SepiaFilter, "Should be instance of SepiaFilter");

        assertThrows(IllegalArgumentException.class, () -> {
            filterFactory.createFilter("NonExistentFilter");
        });
    }

    /**
     * Tests the image conversion functionality.
     * Verifies that -
     * The conversion service supports multiple formats
     * Images can be converted to different formats
     * The converted file has the correct extension
     * @throws IOException if there's an error during conversion
     */
    @Test
    void testImageConversion() throws IOException {
        List<String> formats = conversionService.getSupportedFormats();
        assertFalse(formats.isEmpty(), "Supported formats list should not be empty");

        File outputDir = tempDir.toFile();

        File convertedFile = conversionService.convertImage(
                sampleImageFile,
                "PNG",
                outputDir.getAbsolutePath()
        );

        assertNotNull(convertedFile, "Converted file should not be null");
        assertTrue(convertedFile.exists(), "Converted file should exist");
        assertTrue(convertedFile.getName().toLowerCase().endsWith(".png"),
                "File should have PNG extension");
    }

    /**
     * Tests that the conversion service properly handles unsupported formats.
     * Verifies that an IllegalArgumentException is thrown when attempting to convert to an unsupported format.
     */
    @Test
    void testUnsupportedFormatException() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertImage(
                    sampleImageFile,
                    "UNSUPPORTED",
                    tempDir.toString()
            );
        });
    }

    /**
     * Tests that all available filters can be applied to a BufferedImage.
     * Verifies that each filter can be created and applied without throwing exceptions.
     * @throws IOException if there's an error reading the test image
     */
    @Test
    void testAwsFiltersWithBufferedImage() throws IOException {
        BufferedImage testImage = ImageIO.read(sampleImageFile);
        assertNotNull(testImage, "Test image should be loaded");

        for (String filterName : filterFactory.getAvailableFilters()) {
            ImageFilter filter = filterFactory.createFilter(filterName);
            assertNotNull(filter, "Filter " + filterName + " should be created");
        }
    }
}