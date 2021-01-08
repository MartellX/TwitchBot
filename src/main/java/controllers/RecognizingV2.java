package controllers;



import api.RecognizingV2API;
import api.UnofficialTwitchApi;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FFMpegUtil;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class RecognizingV2 {
    // Sampling
    public static final float sampleRate = 16000;
    public static final int sampleSizeInBits = 16;
    public static final int channels = 1; // Mono
    public static final boolean signed = true;
    public static final boolean bigEndian = false;


    // FFT size
    public static final int chunkSize = 128; // Must be power of 2 for FFT

    public static void main(String[] args) {
        RecognizingV2 recognizingV2 = new RecognizingV2();
        Scanner scanner = new Scanner(System.in);
        while (true) {
                String line = scanner.nextLine();
                if (line.matches("0")) {
                    break;
                } else {
                    String channel = line;
                    new Thread(() -> {
                        String lastResult = "";
                        for (int i = 0; i < 100; i++) {
                            List<File> tsFiles = FFMpegUtil.urlsToTSfiles(new M3U8Controller().getLastTsUrls(channel, 7));
                            String result = recognizingV2.recognize(tsFiles);
                            if (result != null && !result.equals(lastResult)) {
                                System.out.println(channel + ": " + result);
                                lastResult = result;
                            }
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                        }
                    }).start();
                }
        }
//        List<File> tsFiles = FFMpegUtil.urlsToTSfiles(new M3U8Controller().getLastTsUrls("monstercat", 7));
//        System.out.println(recognizingV2.recognize(tsFiles));
    }

    public String recognize(List<File> tsFiles) {

        AudioAttributes audioAttributes = new AudioAttributes()
                .setSamplingRate((int) sampleRate)
                .setChannels(channels)
                .setVolume(512);
        String format = (signed ? "s" : "u") + sampleSizeInBits;
        if (sampleSizeInBits != 8) {
            format += (bigEndian ? "be" : "le");
        }
        audioAttributes.setCodec("pcm_" + format);
        EncodingAttributes encodingAttributes = new EncodingAttributes()
                .setAudioAttributes(audioAttributes)
                .setFormat(format);
//                .setOutputFormat(format);
        File rawFile = FFMpegUtil.encodeFilestoPCM(tsFiles,encodingAttributes);

        short[] dataFromFile = getData(rawFile);

        if (dataFromFile == null) return null;

        DecodedSignature generatedSignature = generateSignature(dataFromFile);

        String result = RecognizingV2API.recognize_from_signature(generatedSignature);

        return result;

    }

    private short[] getData (File file) {
        try(FileInputStream fis = new FileInputStream(file)){
            AudioFormat af = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            byte[] data = fis.readAllBytes();
            int k = sampleSizeInBits / 8;
            try(AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), af, data.length / af.getFrameSize());){
                int length = data.length ;
                short[] audioData = new short[length / k];

                for (int i = 0; i < audioData.length; i++) {
                    byte[] bytes = new byte[2];
                    ais.read(bytes);
                    short value;
                    if (bigEndian) {
                        value = (short) (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
                    } else {
                        value = (short) (((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
                    }
                    audioData[i] = value;
                }

                return audioData;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);

    static double[] HANNING_WINDOW_MULTIPLIERS = new double[2048];
    static{
        for (int i = 0; i < HANNING_WINDOW_MULTIPLIERS.length; i++) {
            HANNING_WINDOW_MULTIPLIERS[i] = (0.5 * (1 - Math.cos((2 * Math.PI * (i + 1)) / 2049)));

        }
    }


    private DecodedSignature generateSignature (short[] audioData) {

        int totalSize = audioData.length;
        int totalChunks = totalSize / chunkSize;

        SignatureGenerator signatureGenerator = new SignatureGenerator() {
            {
                signature = new DecodedSignature() {
                    {
                        sample_rate_hz = (int) sampleRate;
                        number_samples = audioData.length;
                    }
                };
            }
        };

        for (int i = 0; i < totalChunks; i++) {
            signatureGenerator.chunk_index = i;
            float[] lastStripe = do_fft(audioData, signatureGenerator);

            do_peak_spreading(lastStripe, signatureGenerator);
            signatureGenerator.spread_fft_outputs_index++;
            signatureGenerator.spread_fft_outputs_index %= 256;

            signatureGenerator.num_spread_ffts_done++;
            if (signatureGenerator.num_spread_ffts_done >= 46) {
                do_peak_recognition(signatureGenerator);
            }

        }

        return signatureGenerator.signature;
    }

    private float[] do_fft(short[] audioData, SignatureGenerator signatureGenerator) {
        for (int j = 0; j < chunkSize; j++) {
            signatureGenerator.ring_buffer_of_samples[signatureGenerator.ring_buffer_of_samples_index + j] =
                    audioData[signatureGenerator.chunk_index * chunkSize + j];
        }
        signatureGenerator.ring_buffer_of_samples_index += chunkSize;
        signatureGenerator.ring_buffer_of_samples_index %= 2048;

        for (int j = 0; j < 2048; j++) {
            signatureGenerator.reordered_ring_buffer_of_samples[j] =
                    signatureGenerator.ring_buffer_of_samples[(j + signatureGenerator.ring_buffer_of_samples_index) % 2048] *
                            HANNING_WINDOW_MULTIPLIERS[j];
        }
        Complex[] complex_fft_results = FFT.transform(signatureGenerator.reordered_ring_buffer_of_samples, TransformType.FORWARD);


        int binCount = complex_fft_results.length / 2 + 1; // is 1025 actually, but if i forgot how it turns out, that the expression
        float[] stripe = signatureGenerator.fft_outputs[signatureGenerator.fft_outputs_index];

        for (int j = 0; j < binCount; j++) {
            stripe[j] = Math.max(
                    (float) (
                            (Math.pow(complex_fft_results[j].getReal(), 2) +
                                    Math.pow(complex_fft_results[j].getImaginary(), 2))
                                    / (1 << 17)), 0.0000000001f);
        }

        signatureGenerator.fft_outputs_index++;
        signatureGenerator.fft_outputs_index %= 256;

        return stripe.clone();
    }

    private void do_peak_spreading(float[] spread_last_fft, SignatureGenerator signatureGenerator) {
        signatureGenerator.spread_fft_outputs[signatureGenerator.spread_fft_outputs_index] = spread_last_fft;

        for (int position = 0; position < 1025; position++) {

//                    Perform frequency-domain spreading of peak values

            if (position < 1023) {
                spread_last_fft[position] = Math.max(
                        Math.max(spread_last_fft[position], spread_last_fft[position + 1]),
                        spread_last_fft[position + 2]);
            }

//                    Perform time-domain spreading of peak values

            float max_value = spread_last_fft[position];
            for (int former_fft_num: new int[]{-1, -3, -6}
            ) {
                int pos = Math.floorMod(signatureGenerator.spread_fft_outputs_index + former_fft_num, 256);
                float[] former_fft_output = signatureGenerator.spread_fft_outputs[pos];
                max_value = Math.max(former_fft_output[position], max_value);
                former_fft_output[position] = max_value;
            }
        }
    }

    private void do_peak_recognition(SignatureGenerator signatureGenerator) {

        float[] fft_minus_46 = signatureGenerator.fft_outputs[Math.floorMod(
                signatureGenerator.fft_outputs_index - 46, 256)];
        float[] fft_minus_49 = signatureGenerator.spread_fft_outputs[Math.floorMod(
                signatureGenerator.spread_fft_outputs_index - 49, 256)];

        for (int bin_position = 10; bin_position < 1015; bin_position++){

            // Check for size of bin
            if (fft_minus_46[bin_position] >= 1f / 64f &&
                    fft_minus_46[bin_position] >= fft_minus_49[bin_position - 1]) {

                float max_neighbor_in_fft_minus_49 = 0;
                for (int neighbor_offset:new int[]{-10, -7, -4, -3, 1, 2, 5, 8}
                ) {
                    max_neighbor_in_fft_minus_49 = Math.max(
                            fft_minus_49[bin_position + neighbor_offset],
                            max_neighbor_in_fft_minus_49);
                }

                if (fft_minus_46[bin_position] > max_neighbor_in_fft_minus_49) {

                    float max_neighbor_in_other_adjacent_ffts = max_neighbor_in_fft_minus_49;
                    for (int other_offset:new int[] {-53, -45,
                            165, 172, 179, 186, 193, 200,
                            214, 221, 228, 235, 242, 249}
                    ) {

                        var other_fft = signatureGenerator.spread_fft_outputs[
                                Math.floorMod(signatureGenerator.fft_outputs_index + other_offset, 256)
                                ];
                        max_neighbor_in_other_adjacent_ffts =Math.max(other_fft[bin_position - 1],
                                max_neighbor_in_other_adjacent_ffts);
                    }

                    if (fft_minus_46[bin_position] > max_neighbor_in_other_adjacent_ffts) {

                        // This is a peak
                        int fft_pass_number = signatureGenerator.num_spread_ffts_done - 46;

                        float peak_magnitude = (float) (Math.log(
                                Math.max(fft_minus_46[bin_position], 1f / 64f)) *
                                1477.3f + 6144f);
                        float peak_magnitude_before = (float) (Math.log(
                                Math.max(fft_minus_46[bin_position - 1], 1f / 64f)) *
                                1477.3f + 6144f);
                        float peak_magnitude_after = (float) (Math.log(
                                Math.max(fft_minus_46[bin_position + 1], 1.0 / 64.0)) *
                                1477.3f + 6144f);

                        float peak_variation_1 = peak_magnitude * 2 - peak_magnitude_before - peak_magnitude_after;
                        float peak_variation_2 = (peak_magnitude_after - peak_magnitude_before) * 32 / peak_variation_1;

                        int corrected_peak_frequency_bin = (int) (bin_position * 64 + peak_variation_2);

                        assert peak_variation_1 > 0;

                        float frequency_hz = corrected_peak_frequency_bin * (16000f / 2f / 1024f / 64f);

                        FrequencyBand frequencyBand = FrequencyBand.getFrequncyBand((int) frequency_hz);
                        if (frequencyBand == null) {
                            continue;
                        }

                        if (!signatureGenerator.signature.frequency_band_to_sound_peaks.containsKey(frequencyBand)) {
                            signatureGenerator.signature.frequency_band_to_sound_peaks.put(frequencyBand, new ArrayList<>());
                        }

                        signatureGenerator.signature.frequency_band_to_sound_peaks
                                .get(frequencyBand)
                                .add(new FrequencyPeak(fft_pass_number,
                                        (int) peak_magnitude,
                                        corrected_peak_frequency_bin,
                                        (int) sampleRate)
                                );
                    }

                }
            }
        }

    }
}









class SignatureGenerator {
    int chunk_index = 0;

    short[] ring_buffer_of_samples = new short[2048];
    int ring_buffer_of_samples_index = 0;
    double[] reordered_ring_buffer_of_samples = new double[2048];

    float[][] fft_outputs = new float[256][1025];
    int fft_outputs_index = 0;

    float[][] spread_fft_outputs = new float[256][1025];
    int spread_fft_outputs_index = 0;

    int num_spread_ffts_done = 0;

    DecodedSignature signature = new DecodedSignature();



}


enum FrequencyBand {
    _250_520,
    _520_1450,
    _1450_3500,
    _3500_5500;

    public static FrequencyBand getFrequncyBand(int frequency_hz) {
        if (frequency_hz < 250){
            return null;
        } else if (frequency_hz < 520) {
            return FrequencyBand._250_520;
        } else if (frequency_hz < 1450) {
            return FrequencyBand._520_1450;
        } else if (frequency_hz < 3500) {
            return FrequencyBand._1450_3500;
        } else if (frequency_hz <= 5500) {
            return FrequencyBand._3500_5500;
        } else {
            return null;
        }
    }
}

class FrequencyPeak {
    public int fft_pass_number;
    public int peak_magnitude;
    public int corrected_peak_frequency_bin;
    public int sample_rate_hz;

    public FrequencyPeak(int fft_pass_number, int peak_magnitude, int corrected_peak_frequency_bin, int sample_rate_hz) {
        this.fft_pass_number = fft_pass_number;
        this.peak_magnitude = peak_magnitude;
        this.corrected_peak_frequency_bin = corrected_peak_frequency_bin;
        this.sample_rate_hz = sample_rate_hz;
    }
}