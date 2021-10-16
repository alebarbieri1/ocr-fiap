import net.coobird.thumbnailator.Thumbnails;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String basePath = System.getProperty("user.dir") + File.separator + "cupons";
    private static final String enhancedPath = basePath + File.separator + "enhanced" + File.separator;

    private static final String tesseractPath = "/usr/local/Cellar/tesseract-lang/4.1.0/share/tessdata";
    private static final Integer qtdCupons = 6;

    public static void main(String[] args) throws Exception {
        OpenCV.loadLibrary();
        for (int i = 1; i <= qtdCupons; i++) {
            var fileName = String.format("cupom%s.jpg", (i <= 9 ? "0" + i : i));
            var imagePath = basePath + File.separator + fileName;
            var cupomFiscal = carregarCupomFiscal(imagePath, fileName);
            logger.info("============================== Parse {} ==============================", fileName);
            logger.info(cupomFiscal != null ? cupomFiscal.toString() : "Ocorreu um erro ao processar o cupom");
        }
    }

    private static CupomFiscal carregarCupomFiscal(String imagePath, String fileName) throws Exception {
        File originalImage = new File(imagePath);
        Tesseract tess4j = new Tesseract();
        tess4j.setLanguage("por");
        tess4j.setDatapath(tesseractPath);
        tess4j.setTessVariable("user_defined_dpi", "300");

        BufferedImage enhancedImage = enhanceImage(originalImage);
        writeEnhancedImage(enhancedImage, fileName);

        try {
            String result = tess4j.doOCR(enhancedImage);
            logger.info("============================== OCR {} ==============================", fileName);
            logger.info(result);

            return CupomFiscalParser.parseConteudoOCR(result);
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage enhanceImage(File originalImage) throws Exception {
        BufferedImage enhancedImage = loadImage(originalImage);
        enhancedImage = increaseImageSize(enhancedImage);
        enhancedImage = convertImageToGrayscale(enhancedImage);
        enhancedImage = increaseContrast(enhancedImage);
        enhancedImage = blurImage(enhancedImage);
        return enhancedImage;
    }

    private static BufferedImage loadImage(File originalImage) throws Exception {
        return ImageIO.read(originalImage);
    }

    private static BufferedImage increaseImageSize(BufferedImage enhancedImage) throws Exception {
        int newWidth = enhancedImage.getWidth() * 2;
        int newHeight = enhancedImage.getHeight() * 2;
        return Thumbnails.of(enhancedImage).size(newWidth, newHeight).asBufferedImage();
    }

    private static BufferedImage convertImageToGrayscale(BufferedImage enhancedImage) {
        return ImageHelper.convertImageToGrayscale(enhancedImage);
    }

    private static BufferedImage increaseContrast(BufferedImage enhancedImage) throws Exception {
        String tempFileName = UUID.randomUUID().toString() + ".jpg";
        File tempFile = writeEnhancedImage(enhancedImage, tempFileName);
        Mat src = Highgui.imread(tempFile.getAbsolutePath());
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        src.convertTo(dest, -1, 1.05, 0);
        tempFile.delete();
        return convertMatToBufferedImage(dest);
    }

    private static BufferedImage convertMatToBufferedImage(Mat matrix) throws IOException {
        MatOfByte mob = new MatOfByte();
        Highgui.imencode(".jpg", matrix, mob);
        byte ba[] = mob.toArray();
        return ImageIO.read(new ByteArrayInputStream(ba));
    }

    private static BufferedImage blurImage(BufferedImage enhancedImage) throws Exception {
        String tempFileName = UUID.randomUUID().toString() + ".jpg";
        File tempFile = writeEnhancedImage(enhancedImage, tempFileName);
        Mat src = Highgui.imread(tempFile.getAbsolutePath());
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.GaussianBlur(src, dest, new Size(1, 1), 0);
        tempFile.delete();

        return convertMatToBufferedImage(dest);
    }

    private static File writeEnhancedImage(BufferedImage enhancedImage, String fileName) throws Exception {
        File newFile = new File(enhancedPath + fileName);
        String imageFormat = fileName.split("\\.")[1];
        ImageIO.write(enhancedImage, imageFormat, newFile);
        return newFile;
    }
}