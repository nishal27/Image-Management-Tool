package com.imagemanager.controller;

import com.imagemanager.model.ImageModel;
import com.imagemanager.service.ImageConversionService;
import com.imagemanager.service.filter.FilterFactory;
import com.imagemanager.service.filter.ImageFilter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Main controller class for the Image Management Tool.
 * Handles all user interactions and manages the UI components.
 */
public class MainController implements Initializable {

    // UI Components
    @FXML private Button btnUpload;
    @FXML private Button btnConvert;
    @FXML private ListView<String> lstFilters;
    @FXML private ListView<String> lstFormats;
    @FXML private TilePane pnlThumbnails;
    @FXML private ImageView imgPreview;
    @FXML private StackPane previewContainer;
    @FXML private ScrollPane scrollPreview;
    @FXML private TableView<PropertyItem> tblProperties;
    @FXML private TableColumn<PropertyItem, String> colProperty;
    @FXML private TableColumn<PropertyItem, String> colValue;
    @FXML private Label lblStatus;
    @FXML private ProgressBar progressBar;

    // Application state
    private ObservableList<ImageModel> images = FXCollections.observableArrayList();
    private ImageModel selectedImage;
    private FilterFactory filterFactory;
    private ImageConversionService conversionService;
    private Map<ImageModel, ImageView> thumbnailMap = new HashMap<>();
    private List<File> convertedFiles = new ArrayList<>();
    private ChangeListener<Number> previewResizeListener;

    /**
     * Initializes the controller and sets up all UI components.
     * Called automatically when the FXML is loaded.
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        filterFactory = FilterFactory.getInstance();
        conversionService = new ImageConversionService();

        // Set up UI components
        setupTableColumns();
        setupFiltersList();
        setupFormatsList();
        setupButtons();
        setupThumbnailPane();
        setupPreviewPane();

        // Initialize UI state
        progressBar.setVisible(false);
        lblStatus.setText("Ready");
    }

    /**
     * Sets up the properties table columns with custom cell factories.
     * Configures the appearance and behavior of property and value columns.
     */
    private void setupTableColumns() {
        tblProperties.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colProperty.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProperty.setCellFactory(column -> {
            TableCell<PropertyItem, String> cell = new TableCell<PropertyItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setTextFill(Color.WHITE);
                    }
                }
            };
            return cell;
        });

        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colValue.setCellFactory(column -> {
            TableCell<PropertyItem, String> cell = new TableCell<PropertyItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        setTextFill(Color.WHITE);

                        PropertyItem propertyItem = getTableView().getItems().get(getIndex());
                        if (propertyItem != null && "Path".equals(propertyItem.getName())) {
                            Tooltip tooltip = new Tooltip(item);
                            tooltip.setWrapText(true);
                            tooltip.setMaxWidth(500);
                            setTooltip(tooltip);
                        }
                    }
                }
            };
            return cell;
        });

        colProperty.setPrefWidth(100);
        colValue.setPrefWidth(400);
    }

    /**
     * Configures the preview pane for displaying selected images.
     * Sets up the image view and scroll pane with appropriate properties.
     */
    private void setupPreviewPane() {
        previewContainer.setAlignment(Pos.CENTER);
        scrollPreview.setPannable(true);

        imgPreview.setPreserveRatio(true);
        imgPreview.setSmooth(true);

        previewResizeListener = (observable, oldValue, newValue) -> fitImageToPreviewPane();

        scrollPreview.widthProperty().addListener(previewResizeListener);
        scrollPreview.heightProperty().addListener(previewResizeListener);
    }

    /**
     * Adjusts the preview image size to fit within the preview pane while maintaining aspect ratio.
     * Implements a minimum and maximum scale factor to prevent extreme zooming.
     */
    private void fitImageToPreviewPane() {
        if (imgPreview.getImage() == null) return;

        double paneWidth = scrollPreview.getWidth() - 20;
        double paneHeight = scrollPreview.getHeight() - 20;
        double imageWidth = imgPreview.getImage().getWidth();
        double imageHeight = imgPreview.getImage().getHeight();

        double widthRatio = paneWidth / imageWidth;
        double heightRatio = paneHeight / imageHeight;
        double scaleFactor = Math.min(widthRatio, heightRatio);

        if (scaleFactor < 1.0) {
            imgPreview.setFitWidth(imageWidth * scaleFactor);
            imgPreview.setFitHeight(imageHeight * scaleFactor);
        } else {
            double minScaleFactor = Math.max(0.5, Math.min(scaleFactor, 1.5));
            imgPreview.setFitWidth(imageWidth * minScaleFactor);
            imgPreview.setFitHeight(imageHeight * minScaleFactor);
        }
    }

    /**
     * Initializes the filters list with available image filters.
     * Sets up the selection listener to apply filters when selected.
     */
    private void setupFiltersList() {
        List<String> filters = filterFactory.getAvailableFilters();
        lstFilters.setItems(FXCollections.observableArrayList(filters));

        lstFilters.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && selectedImage != null) {
                applyFilter(newValue);
            }
        });
    }

    /**
     * Sets up the formats list with supported output formats.
     * Configures multiple selection mode for batch conversion.
     */
    private void setupFormatsList() {
        List<String> formats = conversionService.getSupportedFormats();
        lstFormats.setItems(FXCollections.observableArrayList(formats));
        lstFormats.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Configures the action handlers for the upload and convert buttons.
     */
    private void setupButtons() {
        btnUpload.setOnAction(event -> uploadImages());
        btnConvert.setOnAction(event -> convertImages());
    }

    /**
     * Sets up the thumbnail pane with appropriate spacing and padding.
     */
    private void setupThumbnailPane() {
        pnlThumbnails.setPadding(new Insets(10));
        pnlThumbnails.setHgap(10);
        pnlThumbnails.setVgap(10);
    }

    /**
     * Opens a file chooser dialog to select image files for upload.
     * Supports multiple file selection with common image format filters.
     */
    private void uploadImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.tiff"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("TIFF", "*.tiff")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(btnUpload.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            loadImages(files);
        }
    }

    /**
     * Loads the selected image files into the application.
     * Clears existing images and updates the UI with new thumbnails.
     * Shows progress during the loading process.
     * @param files List of image files to load
     */
    private void loadImages(List<File> files) {
        images.clear();
        pnlThumbnails.getChildren().clear();
        thumbnailMap.clear();
        tblProperties.getItems().clear();
        imgPreview.setImage(null);
        selectedImage = null;

        progressBar.setVisible(true);
        progressBar.setProgress(0);

        CompletableFuture.runAsync(() -> {
            int total = files.size();
            int count = 0;

            for (File file : files) {
                ImageModel imageModel = new ImageModel(file);
                images.add(imageModel);

                final int currentCount = ++count;
                javafx.application.Platform.runLater(() -> {
                    addThumbnail(imageModel);
                    progressBar.setProgress((double) currentCount / total);
                    lblStatus.setText("Loading images: " + currentCount + "/" + total);

                    if (currentCount == 1) {
                        selectImage(imageModel);
                    }

                    if (currentCount == total) {
                        progressBar.setVisible(false);
                        lblStatus.setText("Loaded " + total + " images");
                    }
                });
            }
        });
    }

    /**
     * Adds a thumbnail for the given image model to the thumbnail pane.
     * Creates an ImageView with appropriate size and click handler.
     * @param imageModel The image model to create a thumbnail for
     */
    private void addThumbnail(ImageModel imageModel) {
        ImageView thumbnail = new ImageView(imageModel.getThumbnailImage());
        thumbnail.setFitWidth(100);
        thumbnail.setFitHeight(100);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(true);

        Tooltip tooltip = new Tooltip(imageModel.getOriginalFile().getName());
        Tooltip.install(thumbnail, tooltip);

        thumbnail.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> selectImage(imageModel));

        pnlThumbnails.getChildren().add(thumbnail);
        thumbnailMap.put(imageModel, thumbnail);
    }

    /**
     * Selects an image and updates the UI to show its preview and properties.
     * Highlights the selected thumbnail and updates the properties table.
     * @param imageModel The image model to select
     */
    private void selectImage(ImageModel imageModel) {
        selectedImage = imageModel;

        Image displayImage = imageModel.getDisplayImage();
        imgPreview.setImage(displayImage);

        fitImageToPreviewPane();

        updateProperties(imageModel);

        highlightThumbnail(imageModel);

        lstFilters.getSelectionModel().clearSelection();

        lblStatus.setText("Selected: " + imageModel.getOriginalFile().getName());
    }

    /**
     * Highlights the thumbnail of the selected image.
     * Adds a visual border to indicate the selected state.
     * @param imageModel The image model whose thumbnail to highlight
     */
    private void highlightThumbnail(ImageModel imageModel) {
        thumbnailMap.values().forEach(imageView ->
                imageView.setStyle("-fx-effect: null;"));

        ImageView selectedThumbnail = thumbnailMap.get(imageModel);
        if (selectedThumbnail != null) {
            selectedThumbnail.setStyle("-fx-effect: dropshadow(three-pass-box, #ff9800, 5, 0, 0, 0);");
        }
    }

    /**
     * Updates the properties table with information about the selected image.
     * Shows details like dimensions, format, and file path.
     * @param imageModel The image model whose properties to display
     */
    private void updateProperties(ImageModel imageModel) {
        ObservableList<PropertyItem> propertyItems = FXCollections.observableArrayList();

        for (Map.Entry<String, String> entry : imageModel.getProperties().entrySet()) {
            propertyItems.add(new PropertyItem(entry.getKey(), entry.getValue()));
        }

        tblProperties.setItems(propertyItems);
    }

    /**
     * Applies the selected filter to the current image.
     * Creates a new filtered image and updates the preview.
     * @param filterName The name of the filter to apply
     */
    private void applyFilter(String filterName) {
        if (selectedImage == null) {
            lblStatus.setText("No image selected");
            return;
        }

        try {
            lblStatus.setText("Applying " + filterName + " filter...");
            progressBar.setVisible(true);
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            CompletableFuture.supplyAsync(() -> {
                ImageFilter filter = filterFactory.createFilter(filterName);
                return filter.apply(selectedImage.getOriginalImage());
            }).thenAccept(filteredImage -> {
                javafx.application.Platform.runLater(() -> {
                    selectedImage.setDisplayImage(filteredImage);

                    imgPreview.setImage(filteredImage);
                    fitImageToPreviewPane();

                    progressBar.setVisible(false);
                    lblStatus.setText("Applied " + filterName + " filter");
                });
            }).exceptionally(e -> {
                javafx.application.Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblStatus.setText("Error applying filter: " + e.getMessage());
                });
                return null;
            });
        } catch (Exception e) {
            progressBar.setVisible(false);
            lblStatus.setText("Error applying filter: " + e.getMessage());
        }
    }

    /**
     * Converts the selected images to the chosen formats.
     * Shows a directory chooser for selecting the output location.
     * Updates progress during conversion and shows success/failure status.
     */
    private void convertImages() {
        if (images.isEmpty()) {
            lblStatus.setText("No images to convert");
            return;
        }

        ObservableList<String> selectedFormats = lstFormats.getSelectionModel().getSelectedItems();
        if (selectedFormats.isEmpty()) {
            lblStatus.setText("No formats selected for conversion");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File outputDir = directoryChooser.showDialog(btnConvert.getScene().getWindow());

        if (outputDir != null) {
            progressBar.setVisible(true);
            progressBar.setProgress(0);

            convertedFiles.clear();

            int totalConversions = images.size() * selectedFormats.size();
            final int[] completedConversions = {0};
            final int[] successCount = {0};
            final int[] failCount = {0};

            CompletableFuture.runAsync(() -> {
                for (ImageModel imageModel : images) {
                    for (String format : selectedFormats) {
                        try {
                            File convertedFile = convertDisplayImageToFile(
                                    imageModel,
                                    format,
                                    outputDir.getAbsolutePath()
                            );
                            convertedFiles.add(convertedFile);
                            successCount[0]++;
                        } catch (Exception e) {
                            failCount[0]++;
                            e.printStackTrace();
                        } finally {
                            completedConversions[0]++;
                            updateConversionProgress(completedConversions[0], totalConversions, successCount[0], failCount[0]);
                        }
                    }
                }

                if (successCount[0] > 0) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            java.awt.Desktop.getDesktop().open(outputDir);
                        } catch (Exception e) {
                            lblStatus.setText("Error opening output directory: " + e.getMessage());
                        }
                    });
                }
            });
        }
    }

    /**
     * Converts a single image to the specified format and saves it to the output path.
     * Handles the actual image conversion process.
     * @param imageModel The image model to convert
     * @param format The target format for conversion
     * @param outputPath The directory to save the converted image
     * @return The converted file
     * @throws IOException If there's an error during conversion or saving
     */
    private File convertDisplayImageToFile(ImageModel imageModel, String format, String outputPath) throws IOException {
        Image displayImage = imageModel.getDisplayImage();
        int width = (int) displayImage.getWidth();
        int height = (int) displayImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelReader pixelReader = displayImage.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
            }
        }

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

        String fileName = imageModel.getOriginalFile().getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        if (!displayImage.equals(imageModel.getOriginalImage())) {
            baseName += "_filtered";
        }

        return conversionService.convertBufferedImage(bufferedImage, format, outputPath, baseName);
    }

    /**
     * Updates the conversion progress in the UI.
     * Shows the number of completed, successful, and failed conversions.
     * @param completed Number of completed conversions
     * @param total Total number of conversions to perform
     * @param success Number of successful conversions
     * @param fail Number of failed conversions
     */
    private void updateConversionProgress(int completed, int total, int success, int fail) {
        javafx.application.Platform.runLater(() -> {
            double progress = (double) completed / total;
            progressBar.setProgress(progress);
            lblStatus.setText("Converting: " + completed + "/" + total +
                    " (" + success + " successful, " + fail + " failed)");

            if (completed == total) {
                progressBar.setVisible(false);
                lblStatus.setText("Conversion complete: " + success + " successful, " + fail + " failed");
            }
        });
    }

    /**
     * Inner class representing a property item in the properties table.
     * Stores a name-value pair for displaying image properties.
     */
    public static class PropertyItem {
        private final SimpleStringProperty name;
        private final SimpleStringProperty value;

        /**
         * Creates a new property item with the given name and value.
         * @param name The property name
         * @param value The property value
         */
        public PropertyItem(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
        }

        /**
         * @return The property name
         */
        public String getName() {
            return name.get();
        }

        /**
         * @return The property name property
         */
        public SimpleStringProperty nameProperty() {
            return name;
        }

        /**
         * @return The property value
         */
        public String getValue() {
            return value.get();
        }

        /**
         * @return The property value property
         */
        public SimpleStringProperty valueProperty() {
            return value;
        }
    }
}