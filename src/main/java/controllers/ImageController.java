package controllers;

import jdk.jfr.Threshold;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ImageController {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String input = sc.nextLine();
            if (input.equals("0")) {
                break;
            } else {
                try {
                    BufferedImage inputImage = ImageController.getImageFromUrl(input);
                    String result = ImageController.ImageToBraille(inputImage, -1);
                    System.out.println(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static int THRESHOLD_DEFAULT = 127;
    static int asciiXDots = 2, asciiYDots = 4;
    static int asciiWidth = 30, asciiHeight = 12; //for twitch chat
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


    public static String ImageToBraille(BufferedImage inputImage,double threshold) {

        Image image = new BufferedImage(asciiWidth * asciiXDots, asciiHeight * asciiYDots, BufferedImage.TYPE_INT_ARGB);
        double scaledY = (double)(asciiHeight * asciiYDots) / (double)inputImage.getHeight() ;
        double scaledX = (double)(asciiWidth * asciiXDots) / (double)inputImage.getWidth();
        double scaled = (scaledX < scaledY ? scaledX : scaledY - 0.02);
        int width = (int)(inputImage.getWidth() * scaled);
        int height = (int)(inputImage.getHeight() * scaled);
        int offsetX = image.getWidth(null)/2 - width / 2;
        int offsetY = image.getHeight(null)/2 - height / 2;
        image.getGraphics().drawImage(inputImage, offsetX, offsetY, width, height, null);

//        Image image = inputImage.getScaledInstance(asciiWidth * asciiXDots, asciiHeight * asciiYDots, Image.SCALE_AREA_AVERAGING);
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



//        for (int i = 0; i < rgbArray.length; i++){
//            int rgb = rgbArray[i];
//            double blue = rgb & 0xff;
//            double green = (rgb & 0xff00) >> 8;
//            double red = (rgb & 0xff0000) >> 16;
//            int grey = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
//            greyArray[i] = grey;
//            histogram[grey]++;
//
//            sumOfGreys += grey;
//            countOfGreys++;
//        }
//


        int currThreshold = getThreshold(rgbArray);
        currThreshold += currThreshold < THRESHOLD_DEFAULT ? 20 : -20;



        if (threshold == -1) {
            threshold = currThreshold;

            //threshold = THRESHOLD_DEFAULT;
        } else {
            threshold = 255 - (threshold/100) * 255;
        }

        System.out.println("Threshold: " + threshold + "\n");
        StringBuilder result = new StringBuilder();
        Random rd = new Random();
        boolean isBlank = true;
        int lastBlankedRows = 0;
        int blanked = 0;
        int lastBlanked;
        Queue<String> rows = new ArrayDeque<>();
        for (int y = 0; y < asciiHeight * asciiYDots; y += asciiYDots) {
            if (y % 2 == 0) {
                rgbArray = inputImage.getRGB(
                        0,
                        y,
                        inputImage.getWidth(),
                        1,
                        null,
                        0,
                        inputImage.getWidth());
                threshold = getThreshold(rgbArray);
            }

            boolean isBlankLast = true;
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < asciiWidth * asciiXDots; x += asciiXDots) {
                char symbol = ImageData2Braille(
                        inputImage
                        .getRGB(x, y, asciiXDots, asciiYDots, null, 0, asciiXDots)
                        , (int)threshold);

                if ((int)symbol != 10241) {
                    isBlank = false;
                    isBlankLast = false;
                }
                //System.out.print(symbol);
                row.append(symbol);
            }

            //result.append('\n');

            if (isBlankLast) {
                lastBlankedRows++;
            } else {
                lastBlankedRows = 0;
            }
            if (!isBlank) {
                rows.offer(row.toString() + "\n");
            }
            //System.out.println();
        }


        while (rows.peek() != null) {
            if (rows.size() == lastBlankedRows) break;
            result.append(rows.poll());
        }

        return result.toString();
    }

    private static int getThreshold(int[] rgbArray) {
        int sumOfGreys = 0, countOfGreys = 0;
        int countOfWhites = 0;
        for (int i = 0; i < rgbArray.length; i++){
            int rgb = rgbArray[i];
            double blue = rgb & 0xff;
            double green = (rgb & 0xff00) >> 8;
            double red = (rgb & 0xff0000) >> 16;
            int grey = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
            if(grey == 0) {
                if (++countOfWhites >= 10) {
                    countOfWhites = 0;
                    sumOfGreys += grey;
                    countOfGreys++;
                }
            } else {
                sumOfGreys += grey;
                countOfGreys++;
            }
        }
        return (sumOfGreys / countOfGreys);
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
        if (brailleChar == 10240) {
            brailleChar += 1;
        }

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
