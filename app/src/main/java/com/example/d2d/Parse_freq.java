package com.example.d2d;

import android.util.Log;

public class Parse_freq {
    // use Goertzel algorithm to analyse the existence of the given frequency
    private int sample_rate;  // sample rate, given by the caller
    private double target_freq;  // frequency to be checked, given by the caller
    private int n;  // block size N, given by the caller
    private int buf_size;  // the whole array size, given by the caller
    // precomputed parameters
    private double q0 = 0;
    private double q1 = 0;
    private double q2 = 0;
    private double sine = 0;
    private double cosine = 0;
    private double coeff = 0;
    // arrays of signals given by the user
    private short[] shorts;

    public Parse_freq(int sample_rate, double target_freq, int n, int buf_size, short[] shorts) {
        this.sample_rate = sample_rate;
        this.target_freq = target_freq;
        this.n = n;
        this.buf_size = buf_size;
        this.shorts = new short[this.buf_size];
        this.shorts = shorts;
    }

    private void resetGoertzel() {
        this.q1 = 0;
        this.q2 = 0;
    }

    private void initGoertzel() {
        int k;
        double floatN;
        double omega;

        floatN = (double) this.n;
        k = (int) (0.5 + ((floatN * this.target_freq)/this.sample_rate));
        omega = (2.0 * Math.PI * k) / floatN;
        this.sine = Math.sin(omega);
        this.cosine = Math.cos(omega);
        this.coeff = 2.0 * cosine;

        resetGoertzel();
    }

    private void processSample(short sample) {
        q0 = coeff * q1 - q2 + (double) sample;
        q2 = q1;
        q1 = q0;
    }

    public String analyse() {
        initGoertzel();
        for (int index = 0; index < n; index++) {
            short each = shorts[index];
            processSample(each);
            Log.d("CHECKINGGOERTZEL", ""+each);
        }

        double realPart = (q1 - q2 * cosine);
        double imagPart = q2 * sine;
        double amplisqr = realPart * realPart + imagPart * imagPart;  // this is amplitude square
        int log_pos = (int) Math.log10(amplisqr);
        StringBuilder builder = new StringBuilder();
//        builder.append(realPart);
//        builder.append("    ");
//        builder.append(imagPart);
        builder.append((long)amplisqr);
        return builder.toString();
    }

}

// reference: https://www.embedded.com/design/configurable-systems/4024443/The-Goertzel-Algorithm