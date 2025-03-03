import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class ProcesamientoSobel {
    public static void main(String[] args) {
        try {
            int numHilos = 1; 
            ExecutorService executor = Executors.newFixedThreadPool(numHilos);

            File archivo = new File("ImagenesComparativas/1.3GB.png");
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                throw new RuntimeException("Error al cargar la imagen.");
            }

            String nombreBase = archivo.getName().replaceFirst("[.][^.]+$", ""); 
            File carpetaSalida = new File("ImagenesProcesadas");

            System.out.println("Usando " + numHilos + " hilos para el procesamiento.");
            long tiempoInicioParalelo = System.nanoTime();

            Pixeles matriz = new Pixeles(numHilos, imagen);
            Future<BufferedImage> futuroGris = executor.submit(matriz::procesarImagenGris);
            BufferedImage imagenGris = futuroGris.get();
            byte[][] matrizPixelesGrises = matriz.obtenerPixelesGrises(); 

            // Sobel
            Sobel sobel = new Sobel(matrizPixelesGrises, numHilos);
            Future<BufferedImage> futuroSobel = executor.submit(sobel::aplicarSobel);
            BufferedImage imagenSobel = futuroSobel.get();

            long tiempoFinParalelo = System.nanoTime();
            double tiempoSeg = (tiempoFinParalelo - tiempoInicioParalelo) / 1_000_000_000.0;

            guardarImagenAsync(executor, imagenGris, carpetaSalida, nombreBase + "_gris");
            guardarImagenAsync(executor, imagenSobel, carpetaSalida, nombreBase + "_sobel");

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            System.out.printf("Tiempo de procesamiento paralelo: %.6f s%n", tiempoSeg);
        } catch (Exception e) {
            System.err.println("Error al procesar la imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void guardarImagenAsync(ExecutorService executor, BufferedImage imagen, File carpetaSalida, String nombre) {
        executor.submit(() -> {
            try {
                File archivoSalida = new File(carpetaSalida, nombre + ".png");
                ImageIO.write(imagen, "png", archivoSalida);
                System.out.println("Imagen guardada: " + archivoSalida.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error al guardar la imagen '" + nombre + "': " + e.getMessage());
            }
        });
    }
}
