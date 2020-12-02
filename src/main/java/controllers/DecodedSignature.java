package controllers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

public class DecodedSignature {
    String DATA_URI_PREFIX = "data:audio/vnd.shazam.sig;base64,";
    public int sample_rate_hz, number_samples;
    HashMap<FrequencyBand, ArrayList<FrequencyPeak>> frequency_band_to_sound_peaks = new HashMap<>();

    public byte[] encode_to_binary() {


        ByteBuffer byteBuffer = ByteBuffer.allocate(20000).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(0xcafe2580);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0x94119c00);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        int samplerateid = SampleRate.get(sample_rate_hz) << 27;
        byteBuffer.putInt(samplerateid);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(number_samples + (int) (sample_rate_hz * 0.24f));
        byteBuffer.putInt((15 << 19) + 0x40000);

        byteBuffer.putInt(0x40000000);
        byteBuffer.putInt(0);

        SortedMap<FrequencyBand, ArrayList<FrequencyPeak>> sorted_iterator = new TreeMap<>(frequency_band_to_sound_peaks);
        for (var band_and_peaks : sorted_iterator.entrySet()
        ) {
            ByteBuffer peaks_buffer = ByteBuffer.allocate(5000).order(ByteOrder.LITTLE_ENDIAN);
            int fft_pass_number = 0;

            for (var frequency_peak : band_and_peaks.getValue()
            ) {

                assert frequency_peak.fft_pass_number >= fft_pass_number;

                if (frequency_peak.fft_pass_number - fft_pass_number >= 255) {
                    peaks_buffer.put((byte) 0xff);
                    peaks_buffer.putInt(frequency_peak.fft_pass_number);

                    fft_pass_number = frequency_peak.fft_pass_number;
                }
                peaks_buffer.put((byte) (frequency_peak.fft_pass_number - fft_pass_number));

                peaks_buffer.putShort((short) frequency_peak.peak_magnitude);
                peaks_buffer.putShort((short) frequency_peak.corrected_peak_frequency_bin);

                fft_pass_number = frequency_peak.fft_pass_number;
            }

            byte[] peaks_buffer_array = Arrays.copyOf(peaks_buffer.array(), peaks_buffer.position());

            byteBuffer.putInt(0x60030040 + band_and_peaks.getKey().ordinal());
            byteBuffer.putInt(peaks_buffer_array.length);
            byteBuffer.put(peaks_buffer_array);
            for (int i = 0; i < Math.floorMod(Math.floorMod(4 - peaks_buffer_array.length, 4), 4); i++) {
                byteBuffer.put((byte) 0);
            }
        }

        int buffer_size = byteBuffer.position();
        byteBuffer.position(8);
        byteBuffer.putInt(buffer_size - 48);

        byteBuffer.position(48 + 4);
        byteBuffer.putInt(buffer_size - 48);

        byte[] buffer_array = Arrays.copyOfRange(byteBuffer.array(), 8, buffer_size);
        byteBuffer.position(4);
        CRC32 hasher = new CRC32();
        hasher.update(buffer_array);
        long hashValue = hasher.getValue();
        byteBuffer.putInt((int) hasher.getValue());

        return Arrays.copyOf(byteBuffer.array(), buffer_size);
    }

    public String encode_to_uri() {
        byte[] byteCode = encode_to_binary();
        return (DATA_URI_PREFIX + new String(Base64.getEncoder().encode(byteCode), StandardCharsets.US_ASCII));

    }

    public Map<Integer, Integer> SampleRate = Map.of(
            8000, 1,
            11025, 2,
            16000, 3,
            32000, 4,
            44100, 5,
            48000, 6);
}
