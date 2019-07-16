package com.example.d2d;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Tone_sender {

    private int duration;
    private int sample_rate;
    private int num_samples;
    private double sample[];
    private double frequency;
    private byte generated_snd[];

    private AudioTrack audioTrack;

    public Tone_sender(int duration, int sample_rate, double frequency) {
        this.duration = duration;
        this.sample_rate = sample_rate;
        this.num_samples = this.duration * this.sample_rate;
        sample = new double[num_samples];
        this.frequency = frequency;
        this.generated_snd = new byte[2 * num_samples];
    }

    public void generate_tone() {
        // generate the sin values in range [-1, 1]
        for (int i = 0; i < num_samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sample_rate/frequency));
        }
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
        Log.d("CHECKINGSAMPLE", "Start playing");
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, generated_snd.length, AudioTrack.MODE_STATIC);
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
