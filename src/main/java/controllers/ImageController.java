package controllers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageController {

    static int THRESHOLD_DEFAULT = 127;
    static int asciiXDots = 2, asciiYDots = 4;
    static int asciiWidth = 33, asciiHeight = 11; //for twitch chat


    public static BufferedImage getImageFromUrl(String url) throws IOException {
        BufferedImage image = ImageIO.read(new URL(url));
        return image;
    }

    public static String ImageToBraille(BufferedImage inputImage,double threshold) {
        Image image = inputImage.getScaledInstance(asciiWidth * asciiXDots, asciiHeight * asciiYDots, Image.SCALE_AREA_AVERAGING);
        inputImage = toBufferedImage(image);
        double sumOfGreys = 0;
        int[] rgbArray = inputImage.getRGB(0, 0,
                inputImage.getWidth(), inputImage.getHeight(), null, 0, inputImage.getWidth());
        /*
        for (int i = 0; i < rgbArray.length; i++){
            int rgb = rgbArray[i];
            int blue = rgb & 0xff;
            int green = (rgb & 0xff00) >> 8;
            int red = (rgb & 0xff0000) >> 16;
            int grey = (blue + green + red) / 3;
            if (grey == 0) sumOfGreys += 50;
            sumOfGreys += grey;
        }

         */

        double averageGrey = sumOfGreys / (inputImage.getWidth() * inputImage.getHeight());
        //threshold = (int) (averageGrey - 10);

        //ImageIO.write(image, "png", new File("test.png"));

        if (threshold == -1) {
            threshold = THRESHOLD_DEFAULT;
        } else {
            threshold = 255 - (threshold/100) * 255;
        }
        StringBuilder result = new StringBuilder("");
        Random rd = new Random();
        for (int y = 0; y < asciiHeight * asciiYDots; y += asciiYDots) {
            for (int x = 0; x < asciiWidth * asciiXDots; x += asciiXDots) {
                char symbol = ImageData2Braille(
                        inputImage
                        .getRGB(x, y, asciiXDots, asciiYDots, null, 0, asciiXDots)
                        , (int)threshold);

                if ((int)symbol == 10240) {
                    symbol = (char) (10240 + (rd.nextInt(7) == 0 ? 1 : 0) );
                } else if ((int)symbol == 10495) {
                    symbol = (char) (10495 - (rd.nextInt(7) == 0 ? 1 : 0) );
                }
                //System.out.print(symbol);
                result.append(symbol);
            }
            result.append('\n');
            //System.out.println();
        }

        return result.toString();
    }

    private static char ImageData2Braille(int[] data, int threshold) {
        int asciiXDots = 2, asciiYDots = 4;
        //int threshold = 130;

        int[] dots = {data[0], data[2], data[4], data[1], data[3], data[5], data[6], data[7]};
        List<Integer> greyDots = new ArrayList<>();
        StringBuilder boolDots = new StringBuilder();
        for (int i = 0; i < dots.length; i++) {
            int rgb = dots[i];
            double blue = rgb & 0xff;
            double green = (rgb & 0xff00) >> 8;
            double red = (rgb & 0xff0000) >> 16;
            double grey = 0.2126 * red + 0.7152 * green + 0.0722 * blue;

            //String dot = ((blue > threshold) ^ (green > threshold) ^ (red > threshold))  ? "1" : "0";
            String dot = grey > threshold ? "1" : "0";

            boolDots.append(dot);
        }

        boolDots.reverse();
        char brailleChar = (char) (10240 + Integer.parseInt(boolDots.toString(), 2));

        String braileString = String.valueOf(brailleChar);
        return brailleChar;
    }

    private static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
