import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class Pixeles {
    private BufferedImage imagen;
    private BufferedImage imagenGris;
    private byte[][] matrizPixelesGrises;
    private int ancho, alto;
    private ForkJoinPool pool;

    public Pixeles(int numHilos, BufferedImage imagen) {
        this.imagen = imagen;
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        this.matrizPixelesGrises = new byte[ancho][alto];
        this.imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        this.pool = new ForkJoinPool(numHilos); 
    }

    public BufferedImage procesarImagenGris() {
        pool.invoke(new MatrizTarea(0, alto));
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