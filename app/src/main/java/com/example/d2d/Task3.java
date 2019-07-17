package com.example.d2d;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Task3 extends AppCompatActivity {

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
    private Switch switch_inaudible;
    private TextView textView_number_input;
    private TextView textView_number_received;
    private Button button_ready_receive;

    private double[] low_freqs = new double[3];
    private double[] high_freqs = new double[3];
    private double[] low_freqs_ina = new double[3];
    private double[] high_freqs_ina = new double[3];
    private boolean isInaudible = false;
    private boolean isSending = false;

    private Multi_Tone_Sender multi_tone_sender = new Multi_Tone_Sender(2, 44100);

    private Thread recordingThread = null;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private boolean isAnalyzing = false;
    private int recordBufSize;
    private long[] result_arr = new long[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task3);
        low_freqs[0] = 697;
        low_freqs[1] = 770;
        low_freqs[2] = 852;
        high_freqs[0] = 1209;
        high_freqs[1] = 1336;
        high_freqs[2] = 1477;
        low_freqs_ina[0] = 18560;
        low_freqs_ina[1] = 18740;
        low_freqs_ina[2] = 18920;
        high_freqs_ina[0] = 19280;
        high_freqs_ina[1] = 19460;
        high_freqs_ina[2] = 19640;
        button_reset = (Button) findViewById(R.id.button_task3_reset);
        button_number_1 = (Button) findViewById(R.id.button_task3_number_1);
        button_number_2 = (Button) findViewById(R.id.button_task3_number_2);
        button_number_3 = (Button) findViewById(R.id.button_task3_number_3);
        button_number_4 = (Button) findViewById(R.id.button_task3_number_4);
        button_number_5 = (Button) findViewById(R.id.button_task3_number_5);
        button_number_6 = (Button) findViewById(R.id.button_task3_number_6);
        button_number_7 = (Button) findViewById(R.id.button_task3_number_7);
        button_number_8 = (Button) findViewById(R.id.button_task3_number_8);
        button_number_9 = (Button) findViewById(R.id.button_task3_number_9);
        button_ready_receive = (Button) findViewById(R.id.button_task3_receive);
        switch_inaudible = (Switch) findViewById(R.id.switch_task3_turn_high);
        textView_number_input = (TextView) findViewById(R.id.textview_task3_number_input);
        textView_number_received = (TextView) findViewById(R.id.textview_task3_receive_result);
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSending = false;
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
                    Toast.makeText(Task3.this, "Oops, it is currently working...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void send_number(int target_number) {
        if (isSending) {
            return;
        }
        if (multi_tone_sender != null) {
            multi_tone_sender.stop_play();
        }
        button_reset.setBackgroundColor(Color.RED);
        isSending = true;
        Toast.makeText(Task3.this, "Sending...", Toast.LENGTH_SHORT).show();
        if (switch_inaudible.isChecked()) {
            isInaudible = true;
        } else {
            isInaudible = false;
        }
        if (isInaudible) {
            multi_tone_sender.set_frequency(high_freqs_ina[(target_number-1)%3], low_freqs_ina[(target_number-1)/3]);
            multi_tone_sender.play_sound();
        } else {
            multi_tone_sender.set_frequency(high_freqs[(target_number-1)%3], low_freqs[(target_number-1)/3]);
            multi_tone_sender.play_sound();
        }
        textView_number_input.setText("The number you choose is: "+target_number);
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
        long previous_max = 0;
        int previous_pos = 0;
        for (double each : low_freqs) {
            Parse_freq parser = new Parse_freq(44100, each, 1104, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                previous_max = current_max;
                previous_pos = current_pos;
                current_max = result_arr[count];
                current_pos = count;
            } else if (result_arr[count] > previous_max) {
                previous_max = result_arr[count];
                previous_pos = count;
            }
            count++;
        }
        for (double each : high_freqs) {
            Parse_freq parser = new Parse_freq(44100, each, 1104, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                previous_max = current_max;
                previous_pos = current_pos;
                current_max = result_arr[count];
                current_pos = count;
            } else if (result_arr[count] > previous_max) {
                previous_max = result_arr[count];
                previous_pos = count;
            }
            count++;
        }
        for (double each : low_freqs_ina) {
            Parse_freq parser = new Parse_freq(44100, each, 1104, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                previous_max = current_max;
                previous_pos = current_pos;
                current_max = result_arr[count];
                current_pos = count;
            } else if (result_arr[count] > previous_max) {
                previous_max = result_arr[count];
                previous_pos = count;
            }
            count++;
        }
        for (double each : high_freqs_ina) {
            Parse_freq parser = new Parse_freq(44100, each, 1104, recordBufSize, sData);
            result_arr[count] = Long.parseLong(parser.analyse());
            if (result_arr[count] > current_max) {
                previous_max = current_max;
                previous_pos = current_pos;
                current_max = result_arr[count];
                current_pos = count;
            } else if (result_arr[count] > previous_max) {
                previous_max = result_arr[count];
                previous_pos = count;
            }
            count++;
        }
        textView_number_received.setText(""+current_pos+" "+previous_pos);
        int temp_c = current_pos;
        int temp_p = previous_pos;
        if (current_pos > 5) {
            current_pos -= 6;
        }
        if (previous_pos > 5) {
            previous_pos -= 6;
        }
        int larger = (current_pos>previous_pos)?current_pos:previous_pos;
        int smaller = (current_pos<previous_pos)?current_pos:previous_pos;
        if (larger > 5 || larger < 3 || smaller > 3) {
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = openFileOutput("data3.txt", Context.MODE_APPEND);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                String temp_result = "";
                for (long each : result_arr) {
                    temp_result += each;
                    temp_result += " ";
                }
                writer.write(temp_result);
                writer.write("\n");
                writer.close();
            } catch (Exception e) {
                ;
            }
            textView_number_received.setText("Try again");
            isAnalyzing = false;
            return;
        }
        int receiver_num = 0;
        if (smaller == 0) {
            receiver_num = larger + smaller - 2;
        } else if (smaller == 1) {
            receiver_num = larger + smaller;
        } else {
            receiver_num = larger + smaller + 2;
        }
        textView_number_received.setText("The number received is: "+receiver_num);
        isAnalyzing = false;

        // collect some data for analysis
//        FileOutputStream out = null;
//        BufferedWriter writer = null;
//        try {
//            out = openFileOutput("data4.txt", Context.MODE_APPEND);
//            writer = new BufferedWriter(new OutputStreamWriter(out));
//            String temp_result = "";
//            temp_result += receiver_num;
//            temp_result += " ";
//            for (long each : result_arr) {
//                temp_result += each;
//                temp_result += " ";
//            }
//            writer.write(temp_result);
//            writer.write("\n");
//            writer.close();
//        } catch (Exception e) {
//            ;
//        }
    }

}
