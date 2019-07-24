package com.example.d2d;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class Certain_Time_Tone_Sender {

    private double duration;
    private int sleep_time;
    private int sample_rate = 44100;
    private String message;
    private double[] sample;

    private short[] shorts;

    private double frequency_high = 18000;
    private double frequency_low = 17500;
    private double frequency_high2 = 17000;
    private double frequency_low2 = 16500;
    private double frequency_clearh = 19000;
    private double frequency_clearl = 18500;
    private int clear_num = 4;
    private boolean isClearHigh = true;
    private int each_length;

    private double current_freq = 0;

    private Context context;

    private AudioTrack audioTrack;
    private boolean isPlaying = true;

    private int current_index = 0;
    private boolean[] bits = new boolean[8];
    private int boolean_index = 0;
    private int length;
    private boolean isClear = true;
    private int clear_count = 1;
    private boolean isDataTransferred = false;
    private boolean isDataTransferring = false;
    private boolean isFirstRound = false;

    public Certain_Time_Tone_Sender(double duration, final String message, Context context) {
        this.duration = duration * 2 / 3;
        this.sleep_time = (int) (duration * 1000);
        this.each_length = (int) (this.duration * sample_rate);
        this.message = message;
        this.sample = new double[each_length];
        this.shorts = new short[each_length];
        this.length = message.length();
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
    }

    private int self_pow(int a, int b) {
        int result = 1;
        for (int i = 0; i < b; i++) {
            result *= a;
        }
        return result;
    }


    private void generate_tone(boolean bit1, boolean bit2, boolean isStart) {
        // generate the sin values in range [-1, 1]
        if (isStart) {
            if (isClearHigh) {
                for (int i = 0; i < sample.length; i++) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/frequency_clearh));
                    isClearHigh = false;
                }
            } else {
                for (int i = 0; i < sample.length; i++) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/frequency_clearl));
                    isClearHigh = true;
                }
            }
            Log.d("ASCII", "Clear");
        } else {
//            if (bit) {
//                current_freq = frequency_high;
//                Log.d("ASCII", "1");
//            } else {
//                current_freq = frequency_low;
//                Log.d("ASCII", "0");
//            }
            if (bit1 && bit2) {
                current_freq = frequency_high;
            } else if (bit1 && (! bit2)) {
                current_freq = frequency_high2;
            } else if ((! bit1) && bit2) {
                current_freq = frequency_low2;
            } else {
                current_freq = frequency_low;
            }
            for (int i = 0; i < sample.length; i++) {
                sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/current_freq));
            }
        }
        int idx = 0;
        for (double dVal : sample) {
            // because 16 bit in range of -32767 to +32767, need to multiple 32767
            short val = (short) (dVal * 32767);
            shorts[idx++] = val;
        }
    }

    public void play_sound() {
        Log.d("ASCII", message);
        isPlaying = true;
        Log.d("CHECKINGSAMPLE", "Start playing");
        clear_count = 0;
        current_index = 0;
        boolean_index = 0;
        while (isClear) {
            generate_tone(true, true,true);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
            audioTrack.write(shorts, 0, shorts.length);
            audioTrack.play();
            try {
                TimeUnit.MILLISECONDS.sleep(sleep_time);
            } catch (Exception e) {
                Log.d("CANNOTSLEEP", "CANNOT SLEEP");
            }
            audioTrack.stop();
            audioTrack.flush();
            audioTrack.release();
            clear_count++;
            if (clear_count >= clear_num) {
                isClear = false;
            }
        }
        isDataTransferring = true;
        isFirstRound = true;
        while (isDataTransferring) {
            if (boolean_index == 8 || isFirstRound) {
                if (current_index == message.length()) {
                    isDataTransferring = false;
                    isClear = true;
                    clear_count = 0;
                    Log.d("asciiascii", "Finish");
                    break;
                } else {
                    int curr = (int) message.charAt(current_index);
                    Log.d("ascii", ""+curr);
                    current_index++;
                    String str = "";
                    for (int i = 0; i < 8; i++) {
                        if (curr >= self_pow(2, 7-i)) {
                            bits[i] = true;
                            curr -= self_pow(2, 7-i);
                            str += "1";
                        } else {
                            bits[i] = false;
                            str += "0";
                        }
                    }
                    Log.d("ASCIIASCII", str);
                    boolean_index = 0;
                    generate_tone(bits[boolean_index], bits[boolean_index+1], false);
//                    audioTrack.flush();
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
                    audioTrack.write(shorts, 0, shorts.length);
                    audioTrack.play();
                    boolean_index++;
                    boolean_index++;
                    isFirstRound = false;
                }
            } else {
                generate_tone(bits[boolean_index], bits[boolean_index+1], false);
//                audioTrack.flush();
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
                audioTrack.write(shorts, 0, shorts.length);
//                Log.d("SPECIALDELIVERY", shorts[0]+"");
                audioTrack.play();
                boolean_index++;
                boolean_index++;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleep_time);
            } catch (Exception e) {
                Log.d("CANNOTSLEEP", "CANNOT SLEEP");
            }
            audioTrack.stop();
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
        Log.d("ASCIIASCII", "Finish_v2");
//        while (isClear) {
//            if (audioTrack != null) {
//                audioTrack.release();
//            }
//            generate_tone(true,true);
//            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
//            audioTrack.write(shorts, 0, shorts.length);
//            try {
//                audioTrack.play();
//            } catch (Exception e) {
//                generate_tone(true, true);
//                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, each_length*2, AudioTrack.MODE_STATIC);
//                audioTrack.write(shorts, 0, shorts.length);
//                audioTrack.play();
//            }
//
//            try {
//                TimeUnit.MILLISECONDS.sleep(sleep_time);
//            } catch (Exception e) {
//                Log.d("CANNOTSLEEP", "CANNOT SLEEP");
//            }
//            audioTrack.flush();
//            audioTrack.stop();
//
//            clear_count++;
//            if (clear_count >= clear_num) {
//                isClear = false;
//            }
//        }
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
