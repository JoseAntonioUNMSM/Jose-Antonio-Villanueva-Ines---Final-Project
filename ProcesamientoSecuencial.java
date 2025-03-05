import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ProcesamientoSecuencial {

    public static void main(String[] args) {
        try {
            File archivo = new File("ImagenesComparativas/293MB.png");
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                throw new RuntimeException("Error al cargar la imagen.");
            }

            String nombreBase = archivo.getName().replaceFirst("[.][^.]+$", "");
            File carpetaSalida = new File("ImagenesProcesadas");
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }

            System.out.println("Procesamiento secuencial.");
            long tiempoInicio = System.nanoTime();

            Pixeles matriz = new Pixeles(1, imagen);
            BufferedImage imagenGris = matriz.procesarImagenGris();
            byte[][] matrizPixelesGrises = matriz.obtenerPixelesGrises();

            Sobel sobel = new Sobel(matrizPixelesGrises, 1);
            BufferedImage imagenSobel = sobel.aplicarSobel();

            guardarImagen(imagenGris, carpetaSalida, nombreBase + "_gris");
            guardarImagen(imagenSobel, carpetaSalida, nombreBase + "_sobel");

            long tiempoFin = System.nanoTime();
            double tiempoSeg = (tiempoFin - tiempoInicio) / 1_000_000_000.0;

            System.out.printf("Tiempo de procesamiento secuencial: %.6f s%n", tiempoSeg);
        } catch (Exception e) {
            System.err.println("Error al procesar la imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void guardarImagen(BufferedImage imagen, File carpetaSalida, String nombre) {
        try {
            File archivoSalida = new File(carpetaSalida, nombre + ".png");
            ImageIO.write(imagen, "png", archivoSalida);
            System.out.println("Imagen guardada: " + archivoSalida.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error al guardar la imagen '" + nombre + "': " + e.getMessage());
        }
    }
}
