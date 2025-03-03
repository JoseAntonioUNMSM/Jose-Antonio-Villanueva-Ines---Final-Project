import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GUI extends JFrame {

    private String originalFileName; // Store the image file name
    private JLabel imageLabel;
    private BufferedImage originalImage, processedImage;
    private int numHilos = 5;
    private byte[][] matrizPixelesGrises;
    private Pixeles matrizThread;
    private double scale = 1.0;
    private JScrollPane scrollPane;

    public GUI() {
        setTitle("Aplicación de Filtros de Imagen");
        setSize(1200, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton selectButton = new JButton("Seleccionar Imagen");
        JButton grayscaleButton = new JButton("Escala de Grises");
        JButton sepiaButton = new JButton("Filtro Sepia");
        JButton sobelButton = new JButton("Filtro Sobel");
        JButton saveButton = new JButton("Guardar Imagen");

        buttonPanel.add(selectButton);
        buttonPanel.add(grayscaleButton);
        buttonPanel.add(sepiaButton);
        buttonPanel.add(sobelButton);
        buttonPanel.add(saveButton);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        scrollPane = new JScrollPane(imageLabel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setLayout(new GridBagLayout());

        imageLabel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getPreciseWheelRotation() < 0) {
                        scale *= 1.1;
                    } else {
                        scale /= 1.1;
                    }
                    updateImageDisplay();
                }
            }
        });

        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        selectButton.addActionListener(e -> selectImage());
        grayscaleButton.addActionListener(e -> applyGrayscale());
        sepiaButton.addActionListener(e -> applySepia());
        sobelButton.addActionListener(e -> applySobel());
        saveButton.addActionListener(e -> saveImage("procesada"));
    }

    private void selectImage() {
        FileDialog fileDialog = new FileDialog(this, "Seleccionar Imagen", FileDialog.LOAD);
        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
        fileDialog.setVisible(true);
    
        String selectedFile = fileDialog.getFile();
        if (selectedFile != null) {
            try {
                File file = new File(fileDialog.getDirectory(), selectedFile);
                originalImage = ImageIO.read(file);
                originalFileName = selectedFile.replaceFirst("[.][^.]+$", ""); // Store file name without extension
                processedImage = null;
                adjustInitialScale();
                updateImageDisplay();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen");
            }
        }
    }

    private void adjustInitialScale() {
        if (originalImage != null) {
            double widthScale = (double) scrollPane.getWidth() / originalImage.getWidth();
            double heightScale = (double) scrollPane.getHeight() / originalImage.getHeight();
            scale = Math.min(widthScale, heightScale) * 0.9; // Scale to fit within the panel
        }
    }
    
    

    private void applyGrayscale() {
        if (originalImage == null) return;
        matrizThread = new Pixeles(numHilos, originalImage);
        processedImage = matrizThread.procesarImagenGris();
        matrizPixelesGrises = matrizThread.obtenerPixelesGrises();
        updateImageDisplay();
    }

    private void applySepia() {
        if (originalImage == null) return;
        MatrizRGB extractor = new MatrizRGB(originalImage, numHilos);
        byte[][][] matrizRGB = extractor.obtenerMatrizRGB();
        SepiaThread sepia = new SepiaThread(matrizRGB, numHilos);
        processedImage = sepia.aplicarSepia();
        updateImageDisplay();
    }

    private void applySobel() {
        if (originalImage == null || matrizPixelesGrises == null) return;
        Sobel sobel = new Sobel(matrizPixelesGrises, numHilos);
        processedImage = sobel.aplicarSobel();
        updateImageDisplay();
    }

    private void saveImage(String filtro) {
        if (processedImage == null || originalFileName == null) return;
        try {
            // Create output folder if it doesn't exist
            File carpetaSalida = new File("ImagenesProcesadas");
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }
    
            String nombreImagenSalida = "ImagenesProcesadas/" + originalFileName + "_" + filtro + "_GUI_" + System.currentTimeMillis() + ".png";
    
            File output = new File(nombreImagenSalida);
            ImageIO.write(processedImage, "png", output);
    
            JOptionPane.showMessageDialog(this, "Imagen guardada como: " + nombreImagenSalida);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la imagen");
        }
    }
    
    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    private void updateImageDisplay() {
        BufferedImage imageToDisplay = (processedImage != null) ? processedImage : originalImage;
        if (imageToDisplay != null) {
            int newWidth = (int) (imageToDisplay.getWidth() * scale);
            int newHeight = (int) (imageToDisplay.getHeight() * scale);
            BufferedImage scaledImage = resizeImage(imageToDisplay, newWidth, newHeight);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setPreferredSize(new Dimension(newWidth, newHeight));
            imageLabel.revalidate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}