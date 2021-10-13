import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    private static final String basePath = "C:\\_temp\\fiap";
    private static final String grayscalePath = basePath + "\\grayscale\\";
    private static final String tesseractPath = "C:\\Users\\DCS0320\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata";

    public static void main(String[] args){
        var listaImagens = new String[] { basePath + "\\cupom01.jpg", basePath + "\\cupom02.jpg", basePath + "\\cupom03.jpg" };
        for(var caminhoImagem : listaImagens){
            var cupomFiscal = carregarCupomFiscal(caminhoImagem);
            if (cupomFiscal != null)
                System.out.println(cupomFiscal);
        }
    }

    private static CupomFiscal carregarCupomFiscal(String imagePath){
        boolean convertToGrayscale = true;

        File image = new File(imagePath);
        Tesseract tess4j = new Tesseract();
        tess4j.setLanguage("por");
        tess4j.setDatapath(tesseractPath);

        try {
            String result = tess4j.doOCR(convertToGrayscale ? convertImageToGrayscale(image) : image);
            return CupomFiscalParser.parseConteudoOCR(result);
        } catch (Exception e){
            return null;
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

        File output = new File(grayscalePath + originalImage.getName());
        ImageIO.write(image, originalImage.getName().split("\\.")[1], output);
        return output;
    }

    private static File convertImageToGrayscaleWithImageHelper(File originalImage) throws Exception {
        BufferedImage newImage = ImageHelper.convertImageToGrayscale(ImageIO.read(originalImage));
        File output = new File(grayscalePath + originalImage.getName());
        ImageIO.write(newImage, originalImage.getName().split("\\.")[1], output);
        return output;
    }
}