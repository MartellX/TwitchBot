package controllers;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ImageController {

    static int THRESHOLD_DEFAULT = 127;
    static int asciiXDots = 2, asciiYDots = 4;
    static int asciiWidth = 33, asciiHeight = 11; //for twitch chat
    static OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(new File("http_cache"), 50L * 1024L * 1024L))
            .build();

    public static BufferedImage getImageFromUrl(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();

        BufferedImage image = ImageIO.read(response.body().byteStream());
        return image;
    }

    public static void main (String[] args) {
        String test = "5Head.png";
        try {
            BufferedImage bi = ImageIO.read(new File(test));
            System.out.println(ImageToBraille(bi, 45));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String ImageToBraille(BufferedImage inputImage,double threshold) {

        Image image = new BufferedImage(asciiWidth * asciiXDots, asciiHeight * asciiYDots, BufferedImage.TYPE_INT_ARGB);
        double scaledY = (double)(asciiHeight * asciiYDots) / (double)inputImage.getHeight() ;
        double scaledX = (double)(asciiWidth * asciiXDots) / (double)inputImage.getWidth();
        double scaled =  (scaledX < scaledY ? scaledX : scaledY - 0.02);
        int width = (int) (inputImage.getWidth() * scaled);
        int height = (int)(inputImage.getHeight() * scaled);
        int offsetX = image.getWidth(null)/2 - width / 2;
        int offsetY = image.getHeight(null)/2 - height / 2;
        image.getGraphics().drawImage(inputImage, offsetX, offsetY, width, height, null);
        inputImage = toBufferedImage(image);
        try {
            ImageIO.write(inputImage, "png", new File("result.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        double sumOfGreys = 0;
        int countOfGreys = 0;
        int[] histogram = new int[256];
        int[] maxValues = {0, 1, 2, 3};
        int[] rgbArray = inputImage.getRGB(0, 0,
                inputImage.getWidth(), inputImage.getHeight(), null, 0, inputImage.getWidth());
        int[] greyArray = new int[rgbArray.length];

        for (int i = 0; i < rgbArray.length; i++){
            int rgb = rgbArray[i];
            double blue = rgb & 0xff;
            double green = (rgb & 0xff00) >> 8;
            double red = (rgb & 0xff0000) >> 16;
            int grey = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
            greyArray[i] = grey;
            histogram[grey]++;

            sumOfGreys += grey;
            countOfGreys++;
        }

        int prevR1 = -1, prevR2 = -2;
        int currR1 = 0, currR2 = 0;
        double u1, u2;

        int currThreshold = (int) (sumOfGreys / countOfGreys);

        while (currR1 != prevR1 || currR2 != prevR2) {
            prevR1 = currR1;
            prevR2 = currR2;
            double greyR1 = 0, greyR2 = 0;
            double countR1 = 0, countR2 = 0;
            for (int i = 0; i < greyArray.length; i++){
                int grey = greyArray[i];
                if (grey > currThreshold) {
                    greyR1 += grey;
                    countR1++;
                } else {
                    greyR2 += grey;
                    countR2 ++;
                }
            }
            u1 = (greyR1 / countR1);
            u2 = (greyR2 / countR2);
            currThreshold = (int) ((u1 + u2) /2);
            currR1 = (int) u1;
            currR2 = (int) u2;
        }


        //double averageGrey = sumOfGreys / countOfGreys;


        //ImageIO.write(image, "png", new File("test.png"));

        if (threshold == -1) {
            threshold = currThreshold;
            System.out.println("Threshold: " + threshold + "\n");
            //threshold = THRESHOLD_DEFAULT;
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

//                if ((int)symbol == 10240) {
//                    symbol = (char) (10240 + (rd.nextInt(7) == 0 ? 1 : 0) );
//                } else if ((int)symbol == 10495) {
//                    symbol = (char) (10495 - (rd.nextInt(7) == 0 ? 1 : 0) );
//                }
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
        Random rd = new Random();
        for (int i = 0; i < dots.length; i++) {
            //threshold = rd.nextInt(255);
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
