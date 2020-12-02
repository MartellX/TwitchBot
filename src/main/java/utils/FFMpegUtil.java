package utils;

import ws.schild.jave.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
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



    static float sec = 12;
    public static File encodeToPcm(File inFile, EncodingAttributes attrs) {

        File target = null;
        try {
            MultimediaObject multimediaObject = new MultimediaObject(inFile);
            long inputDuration = multimediaObject.getInfo().getDuration();
            float secDuration = inputDuration / 1000f;


//            AudioAttributes audio = new AudioAttributes();
//            audio.setCodec("pcm_s16be");
//            audio.setChannels(1);
//            audio.setSamplingRate(16000);
//            //audio.setBitRate(8);
//            audio.setVolume(64);
//            EncodingAttributes attrs = new EncodingAttributes();
//            attrs.setOutputFormat("s16be");
//            attrs.setAudioAttributes(audio);
            attrs.setDuration(sec);
            attrs.setOffset(secDuration - sec - 0.1f > 0 ? secDuration - sec - 0.1f : null);

            Encoder encoder = new Encoder();

            String name = "temp" + new Date().getTime();


            target = File.createTempFile(name, ".raw");
            encoder.encode(multimediaObject, target, attrs);

        } catch (EncoderException | IOException e) {
            e.printStackTrace();
        }

        return target;
    }

    public static File encodeFiletoPCM(File file, EncodingAttributes attrs) {

        MultimediaObject mo = new MultimediaObject(file);
        long inputDuration = 0;
        try {
            inputDuration = mo.getInfo().getDuration();
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        float secDuration = inputDuration / 1000f;


//            AudioAttributes audio = new AudioAttributes();
//            audio.setCodec("pcm_s16be");
//            audio.setChannels(1);
//            audio.setSamplingRate(16000);
//            //audio.setBitRate(8);
//            audio.setVolume(64);
//            EncodingAttributes attrs = new EncodingAttributes();
//            attrs.setOutputFormat("s16be");
//            attrs.setAudioAttributes(audio);
        attrs.setDuration(sec);
        attrs.setOffset(secDuration - sec - 0.1f > 0 ? secDuration - sec - 0.1f : null);
        Encoder encoder = new Encoder();

        String name = "temp" + new Date().getTime();
        File target = null;

        try {
            target = File.createTempFile(name, ".raw");
            encoder.encode(mo, target, attrs);

        } catch (EncoderException | IOException e) {
            e.printStackTrace();
        }

        File finalTarget = target;
        Thread deletingThread = new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                finalTarget.delete();
            }
        });
        deletingThread.setDaemon(true);
        deletingThread.start();

        return target;
    }

    public static File encodeFilestoPCM(List<File> tsFiles, EncodingAttributes attrs) {
        List<MultimediaObject> multimediaObjects = new ArrayList<>();
        long inputDuration = 0;
        for (File t : tsFiles) {
            MultimediaObject mo = new MultimediaObject(t);
            multimediaObjects.add(mo);
            try {
                inputDuration += mo.getInfo().getDuration();
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        float secDuration = inputDuration / 1000f;


//            AudioAttributes audio = new AudioAttributes();
//            audio.setCodec("pcm_s16be");
//            audio.setChannels(1);
//            audio.setSamplingRate(16000);
//            //audio.setBitRate(8);
//            audio.setVolume(64);
//            EncodingAttributes attrs = new EncodingAttributes();
//            attrs.setOutputFormat("s16be");
//            attrs.setAudioAttributes(audio);
        attrs.setDuration(sec);
        attrs.setOffset(secDuration - sec - 0.1f > 0 ? secDuration - sec - 0.1f : null);
        Encoder encoder = new Encoder();

        String name = "temp" + new Date().getTime();
        File target = null;

        try {
            target = File.createTempFile(name, ".raw");
            encoder.encode(multimediaObjects, target, attrs);

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

        return target;
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
                InputStream inputTs = null;
                boolean success = false;
                int attemps = 0;
                while (!success && attemps < 6) {
                    try {
                        inputTs = new URL(url).openStream();
                        success = true;
                    } catch (IOException e) {
                        System.out.println("Не удалось получит TS");
                        attemps++;
                    }
                }
                byte[] buffer = inputTs.readAllBytes();
                FileOutputStream fw = new FileOutputStream(tsFile);
                fw.write(buffer);
                inputTs.close();
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            tsFiles.add(tsFile);
        }

        return tsFiles;
    }

    public static List<File> urlsToTSfiles(Queue<String> urls) {
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
