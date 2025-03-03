
import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ForkJoinPool;

public class Sobel {
    private byte[][] matrizPixelesGrises;
    private BufferedImage imagenSobel;
    private int ancho, alto;
    private ForkJoinPool pool;

    public Sobel(byte[][] matrizPixelesGrises, int numHilos) {
        this.matrizPixelesGrises = matrizPixelesGrises;
        this.ancho = matrizPixelesGrises.length;
        this.alto = matrizPixelesGrises[0].length;
        this.imagenSobel = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        this.pool = new ForkJoinPool(numHilos);
    }

    public BufferedImage aplicarSobel() {
        pool.invoke(new SobelTarea(1, alto - 1));
        
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("❌ Error al esperar la terminación del pool de hilos en Sobel: " + e.getMessage());
        }

        return imagenSobel;
    }


    private class SobelTarea extends RecursiveTask<Void> {
        private int inicio, fin;
        private static final int UMBRAL = 100;

        SobelTarea(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        protected Void compute() {
            if (fin - inicio <= UMBRAL) {
                procesar(inicio, fin);
            } else {
                int medio = (inicio + fin) / 2;
                SobelTarea t1 = new SobelTarea(inicio, medio);
                SobelTarea t2 = new SobelTarea(medio, fin);
                invokeAll(t1, t2);
            }
            return null;
        }

        private void procesar(int inicio, int fin) {
            int[][] kernelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
            };

            int[][] kernelY = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
            };

            for (int y = inicio; y < fin; y++) {
                for (int x = 1; x < ancho - 1; x++) {
                    int Gx = 0, Gy = 0;
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int gris = matrizPixelesGrises[x + i][y + j] & 0xFF;
                            Gx += gris * kernelX[i + 1][j + 1];
                            Gy += gris * kernelY[i + 1][j + 1];
                        }
                    }

                    int gradiente = (int) Math.sqrt(Gx * Gx + Gy * Gy);
                    int colorFinal = (gradiente > 60) ? 255 : 0;
                    imagenSobel.setRGB(x, y, (colorFinal << 16) | (colorFinal << 8) | colorFinal);
                }
            }
        }
    }
}