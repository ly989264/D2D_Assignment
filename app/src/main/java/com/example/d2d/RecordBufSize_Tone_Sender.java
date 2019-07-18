package com.example.d2d;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class RecordBufSize_Tone_Sender {

    private int each_length;
    private int sample_rate = 44100;
    private String message;
    private byte[] generated_snd;
    private double[] sample;

    private double frequency_high = 1000;
    private double frequency_low = 400;
    private double frequency_clear = 1200;

    private double current_freq = 0;

    private Context context;

    private AudioTrack audioTrack;
    private boolean isPlaying = true;

    private int current_index = 0;
    private boolean[] bits = new boolean[8];
    private int boolean_index = 0;
    private int length;

    public RecordBufSize_Tone_Sender(int each_length, final String message, Context context) {
        this.each_length = each_length;
        this.message = message;
        this.generated_snd = new byte[2 * each_length];
        this.sample = new double[each_length];
        this.length = message.length();
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, generated_snd.length, AudioTrack.MODE_STATIC);
        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack audioTrack) {
                Log.d("ASCIIASCII", "Done");
                audioTrack.stop();
                if (!isPlaying) {
                    return;
                }
                Log.d("ASCIIASCII", "Stage 1");
                if (boolean_index == 8) {
                    if (current_index == length) {
                        generate_tone(true,true);
                        audioTrack.setNotificationMarkerPosition(generated_snd.length/2);
                        audioTrack.flush();
                        audioTrack.write(generated_snd, 0, generated_snd.length);
                        audioTrack.play();
                        isPlaying = false;
                        Log.d("ASCIIASCII", "Stage 2");
                    } else {
                        int curr = (int) message.charAt(current_index);
                        current_index++;
                        for (int i = 0; i < 8; i++) {
                            if (curr >= self_pow(2, 7-i)) {
                                bits[i] = true;
                                curr -= self_pow(2, 7-i);
                            } else {
                                bits[i] = false;
                            }
                        }
                        boolean_index = 0;
                        generate_tone(bits[boolean_index],false);
                        audioTrack.setNotificationMarkerPosition(generated_snd.length/2);
                        audioTrack.flush();
                        audioTrack.write(generated_snd, 0, generated_snd.length);
                        audioTrack.play();
                        boolean_index++;
                        Log.d("ASCIIASCII", "Stage 3");
                    }
                } else {
                    generate_tone(bits[boolean_index],false);
                    audioTrack.setNotificationMarkerPosition(generated_snd.length/2);
                    audioTrack.flush();
                    audioTrack.write(generated_snd, 0, generated_snd.length);
                    audioTrack.play();
                    boolean_index++;
                    Log.d("ASCIIASCII", "Stage 4");
                }
            }

            @Override
            public void onPeriodicNotification(AudioTrack audioTrack) {

            }
        });
    }

    private int self_pow(int a, int b) {
        int result = 1;
        for (int i = 0; i < b; i++) {
            result *= a;
        }
        return result;
    }

    private void generate_tone(boolean bit, boolean isStart) {
        if (isStart) {
            // generate the sin values in range [-1, 1]
            for (int i = 0; i < each_length; i++) {
                sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/frequency_clear));
            }
        } else {
            if (bit) {
                current_freq = frequency_high;
            } else {
                current_freq = frequency_low;
            }
            for (int i = 0; i < each_length; i++) {
                sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/current_freq));
            }
        }

//        for (boolean each_bool : bits) {
//            if (each_bool) {
//                current_freq = frequency_high;
//            } else {
//                current_freq = frequency_low;
//            }
//            for (int i = 0; i < num_samples; i++) {
//                sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/current_freq));
//            }
//        }
//        for (int i = 0; i < 2*num_samples; i++) {
//            sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/frequency_clear));
//        }

//        // collect some data for analysis
//        FileOutputStream out = null;
//        BufferedWriter writer = null;
//        try {
//            out = context.openFileOutput("data5.txt", Context.MODE_APPEND);
//            writer = new BufferedWriter(new OutputStreamWriter(out));
//            String temp_result = "";
//            for (double each : sample) {
//                temp_result += each;
//                temp_result += " ";
////                Log.d("ASCIIASCII", ""+each);
//            }
//            writer.write(temp_result);
//            writer.write("\n");
//            writer.close();
//        } catch (Exception e) {
//            Log.d("ASCIIASCII", "Wrong exporting");
//        }

        int idx = 0;
        // generate the 16-bit pcm array
        for (double dVal : sample) {
            // because 16 bit in range of -32767 to +32767, need to multiple 32767
            short val = (short) (dVal * 32767);
            generated_snd[idx++] = (byte) (val & 0x00ff);
            generated_snd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    public void play_sound() {
        isPlaying = true;
        Log.d("CHECKINGSAMPLE", "Start playing");
        this.generate_tone(true,true);
        audioTrack.setNotificationMarkerPosition(generated_snd.length/2);
        audioTrack.write(generated_snd, 0, generated_snd.length);
        audioTrack.play();
        Log.d("CHECKINGSAMPLE", "End playing");
    }

    public void stop_play() {
        if (audioTrack != null) {
            audioTrack.pause();
            audioTrack.release();
            audioTrack = null;
        }
    }

}
