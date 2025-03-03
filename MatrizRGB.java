import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class MatrizRGB {
    private byte[][][] matrizPixelesRGB;
    private BufferedImage imagen;
    private int ancho, alto;
    private ForkJoinPool pool;

    public MatrizRGB(BufferedImage imagen, int numHilos) {
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesRGB = new byte[ancho][alto][3];
        this.pool = new ForkJoinPool(numHilos);
    }

    public byte[][][] obtenerMatrizRGB() {
        pool.invoke(new ExtraccionTarea(0, alto));
        return matrizPixelesRGB;
    }

    private class ExtraccionTarea extends RecursiveTask<Void> {
        private int inicio, fin;
        private static final int UMBRAL = 100; // Tamaño mínimo de división

        ExtraccionTarea(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        protected Void compute() {
            if (fin - inicio <= UMBRAL) {
                procesar(inicio, fin);
            } else {
                int medio = (inicio + fin) / 2;
                ExtraccionTarea t1 = new ExtraccionTarea(inicio, medio);
                ExtraccionTarea t2 = new ExtraccionTarea(medio, fin);
                invokeAll(t1, t2);
            }
            return null;
        }

        private void procesar(int inicio, int fin) {
            for (int y = inicio; y < fin; y++) {
                for (int x = 0; x < ancho; x++) {
                    int pixel = imagen.getRGB(x, y);
                    matrizPixelesRGB[x][y][0] = (byte) ((pixel >> 16) & 0xFF); // Rojo
                    matrizPixelesRGB[x][y][1] = (byte) ((pixel >> 8) & 0xFF);  // Verde
                    matrizPixelesRGB[x][y][2] = (byte) (pixel & 0xFF);         // Azul
                }
            }
        }
    }
}
