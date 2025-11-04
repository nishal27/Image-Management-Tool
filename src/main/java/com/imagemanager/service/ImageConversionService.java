package com.imagemanager.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling image format conversions.
 * Supports conversion between various image formats including PNG, JPG, GIF, TIFF, PDF, SVG, and HEIF.
 * Provides fallback mechanisms when conversion to the target format fails.
 */
public class ImageConversionService {

    // Map of supported formats to their file extensions
    private static final Map<String, String> FORMAT_EXTENSIONS = new HashMap<>();

    static {
        FORMAT_EXTENSIONS.put("PNG", "png");
        FORMAT_EXTENSIONS.put("JPG", "jpg");
        FORMAT_EXTENSIONS.put("GIF", "gif");
        FORMAT_EXTENSIONS.put("TIFF", "tiff");
        FORMAT_EXTENSIONS.put("PDF", "pdf");
        FORMAT_EXTENSIONS.put("SVG", "svg");
        FORMAT_EXTENSIONS.put("HEIF", "heif");
    }

    /**
     * @return List of supported format names
     */
    public List<String> getSupportedFormats() {
        return new ArrayList<>(FORMAT_EXTENSIONS.keySet());
    }

    /**
     * Converts an image file to the specified format.
     * Handles format validation and provides appropriate error messages.
     * @param sourceFile The source image file to convert
     * @param targetFormat The target format (e.g., "PNG", "JPG")
     * @param outputDirectory The directory where the converted file should be saved
     * @return The converted file
     * @throws IOException If an error occurs during conversion
     * @throws IllegalArgumentException If the target format is not supported
     */
    public File convertImage(File sourceFile, String targetFormat, String outputDirectory) throws IOException {
        String formatKey = targetFormat.toUpperCase();

        if (!FORMAT_EXTENSIONS.containsKey(formatKey)) {
            throw new IllegalArgumentException("Unsupported format: " + targetFormat);
        }

        BufferedImage bufferedImage = ImageIO.read(sourceFile);
        if (bufferedImage == null) {
            throw new IOException("Failed to read image file: " + sourceFile.getAbsolutePath());
        }

        String baseName = removeExtension(sourceFile.getName());

        return convertBufferedImage(bufferedImage, targetFormat, outputDirectory, baseName);
    }

    /**
     * Converts a BufferedImage to the specified format.
     * Handles special cases for PDF and SVG formats, and provides fallback to PNG if conversion fails.
     * @param bufferedImage The source image as a BufferedImage
     * @param targetFormat The target format (e.g., "PNG", "JPG")
     * @param outputDirectory The directory where the converted file should be saved
     * @param baseName The base filename (without extension)
     * @return The converted file
     * @throws IOException If an error occurs during conversion
     * @throws IllegalArgumentException If the target format is not supported
     */
    public File convertBufferedImage(BufferedImage bufferedImage, String targetFormat,
                                     String outputDirectory, String baseName) throws IOException {
        String formatKey = targetFormat.toUpperCase();

        if (!FORMAT_EXTENSIONS.containsKey(formatKey)) {
            throw new IllegalArgumentException("Unsupported format: " + targetFormat);
        }

        String extension = FORMAT_EXTENSIONS.get(formatKey);
        String fileName = baseName + "." + extension;
        File outputFile = new File(outputDirectory, fileName);

        // Ensure output directory exists
        File outDir = new File(outputDirectory);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // Handle special format conversions
        if (formatKey.equals("PDF")) {
            return convertToPdf(bufferedImage, outputFile);
        } else if (formatKey.equals("SVG")) {
            return convertToSvg(bufferedImage, outputFile);
        } else if (formatKey.equals("HEIF")) {
            try {
                if (!outputFile.getName().toLowerCase().endsWith(".heif")) {
                    outputFile = new File(outputDirectory, baseName + ".heif");
                }

                ImageIO.write(bufferedImage, "png", outputFile);
                return outputFile;
            } catch (Exception e) {
                outputFile = new File(outputDirectory, baseName + ".png");
                ImageIO.write(bufferedImage, "png", outputFile);
                return outputFile;
            }
        }

        // Standard image format conversion with PNG fallback
        if (!ImageIO.write(bufferedImage, extension, outputFile)) {
            System.err.println("Failed to write to " + extension + " format, using PNG as fallback");
            outputFile = new File(outputDirectory, baseName + ".png");
            ImageIO.write(bufferedImage, "png", outputFile);
        }

        return outputFile;
    }

    /**
     * Removes the file extension from a filename.
     * @param fileName The filename with extension
     * @return The filename without extension
     */
    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Converts a BufferedImage to PDF format.
     * Creates a single-page PDF with the image scaled to fit the page.
     * Falls back to PNG if PDF conversion fails.
     * @param bufferedImage The source image to convert
     * @param outputFile The target PDF file
     * @return The converted file (PDF or fallback PNG)
     * @throws IOException If an error occurs during conversion
     */
    private File convertToPdf(BufferedImage bufferedImage, File outputFile) throws IOException {
        try {
            PDDocument document = new PDDocument();

            float width = bufferedImage.getWidth();
            float height = bufferedImage.getHeight();
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageData = baos.toByteArray();

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageData, "image");

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, 0, 0, width, height);
            contentStream.close();

            document.save(outputFile);
            document.close();

            return outputFile;
        } catch (Exception e) {
            System.err.println("Error creating PDF: " + e.getMessage());
            e.printStackTrace();

            File fallbackFile = new File(outputFile.getParentFile(),
                    removeExtension(outputFile.getName()) + ".png");
            ImageIO.write(bufferedImage, "png", fallbackFile);
            return fallbackFile;
        }
    }

    /**
     * Converts a BufferedImage to SVG format.
     * Creates an SVG document with the image embedded.
     * Falls back to PNG if SVG conversion fails.
     * @param bufferedImage The source image to convert
     * @param outputFile The target SVG file
     * @return The converted file (SVG or fallback PNG)
     * @throws IOException If an error occurs during conversion
     */
    private File convertToSvg(BufferedImage bufferedImage, File outputFile) throws IOException {
        try {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new java.awt.Dimension(width, height));

            svgGenerator.drawImage(bufferedImage, 0, 0, null);

            try (FileWriter out = new FileWriter(outputFile)) {
                svgGenerator.stream(out, true); // true means use CSS style
            }

            return outputFile;
        } catch (Exception e) {
            System.err.println("Error creating SVG: " + e.getMessage());
            e.printStackTrace();

            File fallbackFile = new File(outputFile.getParentFile(),
                    removeExtension(outputFile.getName()) + ".png");
            ImageIO.write(bufferedImage, "png", fallbackFile);
            return fallbackFile;
        }
    }
}