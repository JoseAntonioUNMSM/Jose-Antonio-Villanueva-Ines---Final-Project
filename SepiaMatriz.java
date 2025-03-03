import java.awt.image.BufferedImage;

public class SepiaMatriz implements Runnable {
    private final byte[][][] matrizPixelesRGB;
    private final BufferedImage salida;
    private final int inicio, fin;
    private final int ancho;

    public SepiaMatriz(byte[][][] matrizPixelesRGB, BufferedImage salida, int inicio, int fin) {
        this.matrizPixelesRGB = matrizPixelesRGB;
        this.salida = salida;
        this.inicio = inicio;
        this.fin = fin;
        this.ancho = matrizPixelesRGB.length;
    }

    @Override
    public void run() {
        for (int y = inicio; y < fin; y++) {
            for (int x = 0; x < ancho; x++) {
                int r = matrizPixelesRGB[x][y][0] & 0xFF;
                int g = matrizPixelesRGB[x][y][1] & 0xFF;
                int b = matrizPixelesRGB[x][y][2] & 0xFF;

                // Aplicar la fórmula del filtro Sepia
                int nuevoR = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int nuevoG = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int nuevoB = (int) (0.272 * r + 0.534 * g + 0.131 * b);

                // Limitar valores entre 0 y 255
                nuevoR = Math.min(255, nuevoR);
                nuevoG = Math.min(255, nuevoG);
                nuevoB = Math.min(255, nuevoB);

                // Convertir a formato de píxel RGB
                int pixelSepia = (nuevoR << 16) | (nuevoG << 8) | nuevoB;

                // Escribir en la imagen de salida
                salida.setRGB(x, y, pixelSepia);
            }
        }
    }
}
