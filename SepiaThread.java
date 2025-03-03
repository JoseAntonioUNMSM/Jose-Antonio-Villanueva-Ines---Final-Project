import java.awt.image.BufferedImage;

public class SepiaThread {
    private byte[][][] matrizPixelesRGB;
    private int numHilos;
    private BufferedImage imagenSepia;
    private int ancho, alto;

    public SepiaThread(byte[][][] matrizPixelesRGB, int numHilos) {
        this.matrizPixelesRGB = matrizPixelesRGB;
        this.numHilos = numHilos;
        this.ancho = matrizPixelesRGB.length;
        this.alto = matrizPixelesRGB[0].length;
        this.imagenSepia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage aplicarSepia() {
        Thread[] hilos = new Thread[numHilos];
        int bloque = alto / numHilos; 

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? alto : inicio + bloque;
            hilos[i] = new Thread(new SepiaMatriz(matrizPixelesRGB, imagenSepia, inicio, fin));
            hilos[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return imagenSepia;
    }
}