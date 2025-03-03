import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProcesamientoSepia {
    public static void main(String[] args) {
        try {

            int numHilos = 4;
            File archivoEntrada = new File("Imagenes/frutas.png");
            BufferedImage imagenOriginal = ImageIO.read(archivoEntrada);

            String nombreBase = archivoEntrada.getName().replaceFirst("[.][^.]+$", ""); 
            String nombreImagenSalida = "ImagenesProcesadas/" + nombreBase + "_sepia.png";

            long inicioTiempo = System.nanoTime(); // Tiempo Inicial

            // Extraer la matriz RGB
            MatrizRGB extractor = new MatrizRGB(imagenOriginal, numHilos);
            byte[][][] matrizRGB = extractor.obtenerMatrizRGB();

            // Filtro Sepia
            SepiaThread sepia = new SepiaThread(matrizRGB, numHilos);
            BufferedImage imagenSepia = sepia.aplicarSepia();

            long finTiempo = System.nanoTime(); // Tiempo Final
            double tiempoSegundos = (finTiempo - inicioTiempo) / 1e9; 

            File archivoSalida = new File(nombreImagenSalida);
            ImageIO.write(imagenSepia, "png", archivoSalida);

            System.out.println("Imagen guardada como " + nombreImagenSalida);
            System.out.println("Tiempo de procesamiento: " + tiempoSegundos + " segundos");

        } catch (IOException e) {
            System.err.println("Error al cargar o guardar la imagen: " + e.getMessage());
        }
    }
}
