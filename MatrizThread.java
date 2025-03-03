/*import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class MatrizThread {
    private int numHilos;
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int ancho, alto;
    private ForkJoinPool pool;

    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.numHilos = numHilos;
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_BYTE_GRAY);
        this.pool = new ForkJoinPool(numHilos);
    }

    public BufferedImage procesarImagenGris() {
        pool.invoke(new MatrizTarea(0, alto));

        // ✅ Ahora guardamos la imagen solo cuando se ha procesado completamente
        guardarImagen("imagen_gris_Final.png");

        return imagenGris;
    }

    private class MatrizTarea extends RecursiveTask<Void> {
        private int inicio, fin;
        private static final int UMBRAL = 100;

        MatrizTarea(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        protected Void compute() {
            if (fin - inicio <= UMBRAL) {
                procesar(inicio, fin);
            } else {
                int medio = (inicio + fin) / 2;
                invokeAll(new MatrizTarea(inicio, medio), new MatrizTarea(medio, fin));
            }
            return null;
        }

        private void procesar(int inicio, int fin) {
            final float Red = 0.2126f, Green = 0.7152f, Blue = 0.0722f;

            for (int y = inicio; y < fin; y++) {
                for (int x = 0; x < ancho; x++) {
                    int pixel = imagen.getRGB(x, y);
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;

                    int luminancia = (int) (r * Red + g * Green + b * Blue);
                    byte valorGris = (byte) (luminancia & 0xFF);

                    matrizPixelesGrises[x][y] = valorGris;

                    imagenGris.getRaster().setSample(x, y, 0, luminancia);
                }
            }
        }
    }

    private void guardarImagen(String nombreArchivo) {
        try {
            File archivoSalida = new File("Imagenes/" + nombreArchivo);
            if (!archivoSalida.getParentFile().exists()) {
                archivoSalida.getParentFile().mkdirs();
            }
            ImageIO.write(imagenGris, "png", archivoSalida);
            System.out.println("✅ Imagen en escala de grises guardada en: " + archivoSalida.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Error al guardar la imagen: " + e.getMessage());
        }
    }

    public byte[][] obtenerPixelesGrises() {
        return matrizPixelesGrises;
    }
}*/

import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class MatrizThread {
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int ancho, alto;
    private ForkJoinPool pool;

    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        this.pool = new ForkJoinPool(numHilos); // Crea un pool dinámico de hilos
    }

    public BufferedImage procesarImagenGris() {
        pool.invoke(new MatrizTarea(0, alto));
        return imagenGris;
    }

    private class MatrizTarea extends RecursiveTask<Void> {
        private int inicio, fin;
        private static final int UMBRAL = 100; // Fragmentación mínima

        MatrizTarea(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        protected Void compute() {
            if (fin - inicio <= UMBRAL) {
                procesar(inicio, fin);
            } else {
                int medio = (inicio + fin) / 2;
                MatrizTarea t1 = new MatrizTarea(inicio, medio);
                MatrizTarea t2 = new MatrizTarea(medio, fin);
                invokeAll(t1, t2);
            }
            return null;
        }

        private void procesar(int inicio, int fin) {
            int ancho = imagen.getWidth();
            final float Red = 0.2126f, Green = 0.7152f, Blue = 0.0722f;

            for (int y = inicio; y < fin; y++) {
                for (int x = 0; x < ancho; x++) {
                    int pixel = imagen.getRGB(x, y);
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;

                    int luminancia = (int) (r * Red + g * Green + b * Blue);
                    matrizPixelesGrises[x][y] = (byte) (luminancia & 0xFF);

                    int pixelGris = (luminancia << 16) | (luminancia << 8) | luminancia;
                    imagenGris.setRGB(x, y, pixelGris);
                }
            }
        }
    }

    public byte[][] obtenerPixelesGrises() {
        return matrizPixelesGrises;
    }
}


/*import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrizThread {
    private int numHilos;
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int ancho, alto;

    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.numHilos = numHilos;
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage procesarImagenGris() {
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        int bloque = (int) Math.ceil((double) alto / numHilos);
        int[][] bufferTemporal = new int[ancho][alto]; // Buffer para evitar acceso concurrente

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = Math.min(alto, inicio + bloque); // Asegurar que no exceda el límite

            executor.execute(new MatrizPixeles(imagen, matrizPixelesGrises, bufferTemporal, inicio, fin));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Copiar los valores del buffer temporal a la imagen final
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                imagenGris.setRGB(x, y, bufferTemporal[x][y]);
            }
        }

        return imagenGris;
    }

    public byte[][] obtenerPixelesGrises() {
        return matrizPixelesGrises;
    }
}

/*import java.awt.image.BufferedImage;
import java.util.concurrent.CyclicBarrier;

public class MatrizThread {

    private int numHilos;
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int alto, ancho;
    private CyclicBarrier barrera;

    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.numHilos = numHilos;
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        this.barrera = new CyclicBarrier(numHilos + 1);
    }

    public BufferedImage procesarImagenGris() {
        Thread[] hilos = new Thread[numHilos];
        int bloque = alto / numHilos;

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? alto : inicio + bloque;

            hilos[i] = new Thread(() -> {
                try {
                    new MatrizPixeles(imagen, matrizPixelesGrises, imagenGris, inicio, fin).run();
                    barrera.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            hilos[i].start();
        }

        try {
            barrera.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imagenGris;
    }

    public byte[][] obtenerPixelesGrises() {
        return matrizPixelesGrises;
    }
}





/*import java.awt.image.BufferedImage;
import java.util.concurrent.CyclicBarrier;

public class MatrizThread {

    private int numHilos;
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int[][][] matrizPixelesRGB;
    private int alto, ancho;
    private CyclicBarrier barrera;

    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.numHilos = numHilos;
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesRGB = new int[ancho][alto][3];
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        this.barrera = new CyclicBarrier(numHilos + 1);
    }

    public BufferedImage procesarImagenGris() {
        Thread[] hilos = new Thread[numHilos];

        int bloque = alto / numHilos;

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? alto : inicio + bloque;

            hilos[i] = new Thread(() -> {
                try {
                    new MatrizPixeles(imagen, matrizPixelesRGB,matrizPixelesGrises, imagenGris, inicio, fin).run();
                    barrera.await(); 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            hilos[i].start();
        }

        try {
            barrera.await(); 
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imagenGris;
    }

    public byte[][] obtenerPixelesGrises() {
        return matrizPixelesGrises;
    }

    public int[][][] obtenerPixelesRGB() {
        return matrizPixelesRGB;
    }
}





/*import java.awt.image.BufferedImage;
import java.util.concurrent.CyclicBarrier;

public class MatrizThread {

    private int numHilos;
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private int[][][] matrizPixeles;
    private int alto,ancho;
    private CyclicBarrier barrera;


    public MatrizThread(int numHilos, BufferedImage imagen) {
        this.numHilos = numHilos;
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixeles = new int[ancho][alto][3];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        this.barrera = new CyclicBarrier(numHilos+1);
    }

    public BufferedImage procesarImagenGris() {

        Thread[] hilos = new Thread[numHilos];

        int bloque = alto / numHilos;

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? alto : inicio + bloque;
            
            // Crear hilo con la tarea
            hilos[i] = new Thread(new MatrizPixeles(imagen, matrizPixeles, imagenGris, inicio, fin));
            hilos[i].start();
        }

        // 🔹 Esperar a que todos los hilos terminen
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return imagenGris;
    }

    public int[][][] obtenerPixeles() {
        return matrizPixeles;
    }


}*/
