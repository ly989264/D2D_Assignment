package com.example.d2d;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Task2 extends AppCompatActivity {

    private Button button_reset;
    private Button button_number_1;
    private Button button_number_2;
    private Button button_number_3;
    private Button button_number_4;
    private Button button_number_5;
    private Button button_number_6;
    private Button button_number_7;
    private Button button_number_8;
    private Button button_number_9;
    private Switch switch_use_high_feq;
    private TextView textView_number_input;
    private TextView textView_number_received;
    private Button button_ready_receive;

    private double[] low_freqs = new double[9];
    private double[] high_freqs = new double[9];
    private double low_freq_l = 8000;
    private double low_freq_step = 500;
    private double high_freq_l = 18560;
    private double high_freq_step = 180;

    // tone sender
    private Tone_sender tone_sender;
    private boolean is_sending = false;

    // tone receiver
    private Thread recordingThread = null;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private boolean isAnalyzing = false;
    private int recordBufSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);
        for (int index = 0; index < 9; index++) {
            low_freqs[index] = low_freq_l + index * low_freq_step;
            high_freqs[index] = high_freq_l + index * high_freq_step;
        }
        button_reset = (Button) findViewById(R.id.button_task2_reset);
        button_number_1 = (Button) findViewById(R.id.button_number_1);
        button_number_2 = (Button) findViewById(R.id.button_number_2);
        button_number_3 = (Button) findViewById(R.id.button_number_3);
        button_number_4 = (Button) findViewById(R.id.button_number_4);
        button_number_5 = (Button) findViewById(R.id.button_number_5);
        button_number_6 = (Button) findViewById(R.id.button_number_6);
        button_number_7 = (Button) findViewById(R.id.button_number_7);
        button_number_8 = (Button) findViewById(R.id.button_number_8);
        button_number_9 = (Button) findViewById(R.id.button_number_9);
        switch_use_high_feq = (Switch) findViewById(R.id.switch_turn_high);
        textView_number_input = (TextView) findViewById(R.id.textview_number_input);
        textView_number_received = (TextView) findViewById(R.id.textview_task2_receive_result);
        button_ready_receive = (Button) findViewById(R.id.button_task2_receive);
        button_reset.setBackgroundColor(Color.GRAY);
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                is_sending = false;
                button_reset.setBackgroundColor(Color.GRAY);
            }
        });
        button_number_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(1);
            }
        });
        button_number_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(2);
            }
        });
        button_number_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(3);
            }
        });
        button_number_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(4);
            }
        });
        button_number_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(5);
            }
        });
        button_number_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(6);
            }
        });
        button_number_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(7);
            }
        });
        button_number_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(8);
            }
        });
        button_number_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_number(9);
            }
        });
        button_ready_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (! isAnalyzing) {
                    startRecording();
                } else {
                    Toast.makeText(Task2.this, "Oops, it is currently working...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void send_number(int target_number) {
        if (is_sending) {
            return;
        }
        button_reset.setBackgroundColor(Color.RED);
        boolean is_low_freq;
        if (switch_use_high_feq.isChecked()) {
            is_low_freq = false;
        } else {
            is_low_freq = true;
        }
        double current_freq;
        if (is_low_freq) {
            current_freq = low_freqs[target_number-1];
        } else {
            current_freq = high_freqs[target_number-1];
        }
        try {
            tone_sender = new Tone_sender(3, 44100, current_freq);
            tone_sender.generate_tone();
            tone_sender.play_sound();
        } catch (Exception e) {
            Toast.makeText(Task2.this, "Something wrong while sending tone", Toast.LENGTH_SHORT).show();
        }
        textView_number_input.setText("The number you choose is: "+target_number);
        Toast.makeText(Task2.this, "Sending...", Toast.LENGTH_SHORT).show();
        is_sending = true;
    }

    void startRecording() {
        recordBufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d("TASK2RECORDBUFSIZE", recordBufSize+"");
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                recordBufSize);
        recorder.startRecording();
        isRecording = true;
        isAnalyzing = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile() {
        // I want to record 20 sData arrays
        short sDatas[][] = new short[recordBufSize][20];
        int arr_cnt = 0;
        short sData[] = new short[recordBufSize];
        long result_arr[] = new long[18];
        isAnalyzing = true;

        while (isRecording) {
            // gets the voice output from microphone


            recorder.read(sData, 0, recordBufSize);
            textView_number_received.setText("Recording...");


            sDatas[arr_cnt] = sData;
            arr_cnt++;
            if (arr_cnt == 20) {
                isRecording = false;
                recorder.stop();
                recorder.release();
                recorder = null;
                recordingThread = null;
            }
        }
        Log.d("LISTENINGRESULT", "Done");
        textView_number_received.setText("Analyzing...");
        // first I want to scan for all spectrum in range (100, 22000) with step of 70 (because the recognition range is 80Hz)
        sData = sDatas[10];  // analyze the medium one only
        int count = 0;
        long current_max = 0;
        int current_pos = 0;
        boolean is_low_pos = true;
        for (double freq: low_freqs) {
            Parse_freq parser = new Parse_freq(44100, freq, 552, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                current_max = result_arr[count];
                current_pos = count+1;
                is_low_pos = true;
            }
            count++;
        }
        for (double freq: high_freqs) {
            Parse_freq parser = new Parse_freq(44100, freq, 552, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                current_max = result_arr[count];
                current_pos = count-9+1;
                is_low_pos = false;
            }
            count++;
        }


        // find the maximum value of result_arr, which is the corresponding signal
        textView_number_received.setText("The number received is: "+current_pos+(is_low_pos?"(low freq)":"(high freq)"));
        isAnalyzing = false;

        // this is used to store the data to a file, for analysis
//        FileOutputStream out = null;
//        BufferedWriter writer = null;
//        try {
//            out = openFileOutput("data.txt", Context.MODE_APPEND);
//            writer = new BufferedWriter(new OutputStreamWriter(out));
//            String temp_result = "";
//            for (String each : result_arr) {
//                temp_result += each;
//                temp_result += " ";
//            }
//            writer.write(temp_result);
//            writer.write("\n");
//            writer.close();
//        } catch (Exception e) {
//            ;
//        }
//
//        textView_number_received.setText("Done");
    }

}


// To be further improved:
// cannot know whether there is signal transmitted (threshold needed)