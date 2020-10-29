package utils;

import ws.schild.jave.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FFMpegUtil {

    public static File encodeTStoMP3(List<File> tsFiles) {
        List<MultimediaObject> multimediaObjects = new ArrayList<>();
        tsFiles.forEach(t ->
            multimediaObjects.add(new MultimediaObject(t))
        );
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();

        String name = "temp" + new Date().getTime();
        File targetMP3 = null;

        try {
            targetMP3 = File.createTempFile(name, ".mp3");
            encoder.encode(multimediaObjects, targetMP3, attrs);

        } catch (EncoderException | IOException e) {
            e.printStackTrace();
        }

        Thread deletingThread = new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                tsFiles.forEach(File::delete);
            }
        });
        deletingThread.setDaemon(true);
        deletingThread.start();

        return targetMP3;
    }

    public static List<File> urlsToTSfiles(List<String> urls) {
        List<File> tsFiles = new ArrayList<>();
        int i = 1;
        for (String url:urls
             ) {
            File tsFile = null;
            try {
                tsFile = File.createTempFile("temp" + new Date().getTime() + i,".ts");
                i++;
                InputStream inputTs = new URL(url).openStream();
                byte[] buffer = inputTs.readAllBytes();
                FileOutputStream fw = new FileOutputStream(tsFile);
                fw.write(buffer);
                inputTs.close();
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            tsFiles.add(tsFile);
        }

        return tsFiles;
    }


}
