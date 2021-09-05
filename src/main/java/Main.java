import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args){
        boolean convertToGrayscale = true;

        File image = new File("/Users/AleBarbieri/Documents/FIAP/OCR/cupom-fiscal-5.jpg");
        Tesseract tess4j = new Tesseract();
        tess4j.setLanguage("por");
        tess4j.setDatapath("/usr/local/Cellar/tesseract-lang/4.1.0/share/tessdata");

        try {
            String result = tess4j.doOCR(convertToGrayscale ? convertImageToGrayscale(image) : image);
            System.out.println("Resultado OCR:\n\n" + result);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static File convertImageToGrayscale(File originalImage) throws Exception {
        BufferedImage image = ImageIO.read(originalImage);
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                Color color = new Color(image.getRGB(j, i));
                int red = (int) (color.getRed() * 0.299);
                int green = (int) (color.getGreen() * 0.587);
                int blue = (int) (color.getBlue() * 0.114);
                Color newColor = new Color(
                        red + green + blue,
                        red + green + blue,
                        red + green + blue);
                image.setRGB(j, i, newColor.getRGB());
            }
        }

        File output = new File("/Users/AleBarbieri/Documents/FIAP/OCR/grayscale/" + originalImage.getName());
        ImageIO.write(image, originalImage.getName().split("\\.")[1], output);
        return output;
    }

    private static File convertImageToGrayscaleWithImageHelper(File originalImage) throws Exception {
        BufferedImage newImage = ImageHelper.convertImageToGrayscale(ImageIO.read(originalImage));
        File output = new File("/Users/AleBarbieri/Documents/FIAP/OCR/grayscale/" + originalImage.getName());
        ImageIO.write(newImage, originalImage.getName().split("\\.")[1], output);
        return output;
    }
}