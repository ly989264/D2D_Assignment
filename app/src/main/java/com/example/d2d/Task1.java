package com.example.d2d;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Task1 extends AppCompatActivity {

    private EditText editText_send_frequency;
    private Button button_send_tone;
    private Button button_stop_send;
    private Button button_listen_tone;
    private TextView textView_listen_tone;
    private GraphView graphView;
    private Button button_show_spectrum_graph;
    private Button button_show_detail_graph;

    // tone sender
    private Tone_sender tone_sender = null;
    private double frequency;

    // tone receiver
    private Thread recordingThread = null;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private boolean isAnalyzing = false;
    private boolean spectrum_done = false;
    private boolean detail_done = false;
    private int temp_freq_value;
    private int recordBufSize;
    private DataPoint[] dataPoints_detail;
    private DataPoint[] dataPoints_spectrum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);
        if (ContextCompat.checkSelfPermission(Task1.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(Task1.this, "Oops, permission not granted", Toast.LENGTH_LONG).show();
        }
        editText_send_frequency = (EditText) findViewById(R.id.editText_send_frequency);
        button_send_tone = (Button) findViewById(R.id.button_send_tone);
        button_send_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double freq = Double.parseDouble(editText_send_frequency.getText().toString());
                    frequency = freq;
                    tone_sender = new Tone_sender(3, 44100, frequency);
                    tone_sender.generate_tone();
                    tone_sender.play_sound();
                } catch (Exception e) {
                    Toast.makeText(Task1.this, "Do you enter the correct send frequency format?", Toast.LENGTH_LONG).show();
                }
            }
        });
        button_stop_send = (Button) findViewById(R.id.button_stop_send);
        button_stop_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tone_sender != null) {
                    tone_sender.stop_play();
                }
            }
        });
        button_listen_tone = (Button) findViewById(R.id.button_listen_tone);
        textView_listen_tone = (TextView) findViewById(R.id.textview_listen_tone_freqency);
        button_listen_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((! isRecording) && (! isAnalyzing)) {
                    startRecording();
                    spectrum_done = false;
                    detail_done = false;
                } else {
                    Toast.makeText(Task1.this, "Currently working...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        graphView = (GraphView) findViewById(R.id.graph);
        button_show_spectrum_graph = (Button) findViewById(R.id.button_show_spectrum_graph);
        button_show_detail_graph = (Button) findViewById(R.id.button_show_detail_graph);
        button_show_spectrum_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                draw_spectrum_graph();
            }
        });
        button_show_detail_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                draw_detail_graph();
            }
        });
    }

    void startRecording() {
        recordBufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d("RECORDBUFSIZE", recordBufSize+"");
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                recordBufSize);
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i*2] = (byte) (sData[i] & 0x00FF);
            bytes[(i*2)+1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private String short2String(short[] sData) {
        StringBuilder builder = new StringBuilder();
        for (short each : sData) {
            builder.append(Short.toString(each));
            builder.append(" ");
        }
        return builder.toString();
    }

    private void draw_spectrum_graph() {
        if (spectrum_done) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints_spectrum);
            graphView.removeAllSeries();
            graphView.addSeries(series);

            graphView.getViewport().setMinX(100);
            graphView.getViewport().setMaxX(22000);
            graphView.getViewport().setScalable(true);
            graphView.getViewport().setScalableY(false);
            graphView.getViewport().setScrollableY(false);
//            graphView.getGridLabelRenderer().setLabelHorizontalHeight(10);
            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(120);
            graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graphView.getViewport().setXAxisBoundsManual(true);
//            graphView.getViewport().setYAxisBoundsManual(true);
        }
    }

    private void draw_detail_graph() {
        if (detail_done) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints_detail);
            graphView.removeAllSeries();
            graphView.addSeries(series);

            graphView.getViewport().setMinX(temp_freq_value-150);
            graphView.getViewport().setMaxX(temp_freq_value+150);
            graphView.getViewport().setScalable(true);
            graphView.getViewport().setScalableY(false);
            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(120);
            graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graphView.getViewport().setXAxisBoundsManual(true);
//            graphView.getViewport().setYAxisBoundsManual(true);
        }
    }

    private void writeAudioDataToFile() {
        // I want to record 20 sData arrays
        short sDatas[][] = new short[recordBufSize][20];
        int arr_cnt = 0;
        short sData[] = new short[recordBufSize];
        isAnalyzing = true;

        while (isRecording) {
            // gets the voice output from microphone


            recorder.read(sData, 0, recordBufSize);
            textView_listen_tone.setText("Recording...");


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
        textView_listen_tone.setText("Analyzing...");
        // first I want to scan for all spectrum in range (100, 22000) with step of 70 (because the recognition range is 80Hz)
        sData = sDatas[10];  // analyze the medium one only
        dataPoints_spectrum = new DataPoint[313];
        int freq_l = 100;
        int freq_u = 22000;
        int step = 70;
        int cnt = 0;
        long result_value = 0;
        long temp_biggest = 0;
        int temp_freq = 0;
        int current_index = 0;
        // temp, write to file
        FileOutputStream out = null;
        BufferedWriter writer = null;

        while (freq_l < freq_u) {
            cnt++;
            if (cnt == 3) {
                freq_l += step;
                cnt = 0;
                try {
                    out = openFileOutput("data.txt", Context.MODE_APPEND);
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    result_value /= 3;
                    String v = result_value + "";
                    writer.write(v);
                    writer.write(" ");
                    writer.close();
                    if (current_index < 313) {
                        dataPoints_spectrum[current_index] = new DataPoint(freq_l, result_value);
                    }
                    current_index++;
                } catch (Exception e) {
                    ;
                }
                if (result_value > temp_biggest) {
                    temp_biggest = result_value;
                    temp_freq = freq_l;
                }
                result_value = 0;
            }
            Log.d("TESTINGRECORDING", short2String(sData));
            Parse_freq parser = new Parse_freq(44100, freq_l, 552, recordBufSize, sData);
            String result = parser.analyse();
//            textView_listen_tone.setText(""+freq_l+" "+result);
            Log.d("TESTINGRECORDING", result);

            result_value+=Long.parseLong(result);
        }
        spectrum_done = true;

        // then, need to reduce the step to 2Hz and analyze the specturm in range of (temp_freq-150, temp_frep+150)
        freq_l = temp_freq-150;
        freq_u = temp_freq+150;
        step = 2;
        cnt = 0;
        result_value = 0;
        dataPoints_detail = new DataPoint[150];
        current_index = 0;
        // temp, write to file
        out = null;
        writer = null;

        while (freq_l < freq_u) {
            cnt++;
            if (cnt == 3) {
                freq_l += step;
                cnt = 0;
                try {
                    out = openFileOutput("data2.txt", Context.MODE_APPEND);
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    result_value /= 3;
                    String v = result_value + "";
                    writer.write(v);
                    writer.write(" ");
                    writer.close();
                    if (current_index < 150) {
                        dataPoints_detail[current_index] = new DataPoint(freq_l, result_value);
                    }
                    current_index++;
                } catch (Exception e) {
                    ;
                }
                result_value = 0;
            }
            Log.d("TESTINGRECORDING", short2String(sData));
            Parse_freq parser = new Parse_freq(44100, freq_l, 552, recordBufSize, sData);
            String result = parser.analyse();
//            textView_listen_tone.setText(""+freq_l+" "+result);
            Log.d("TESTINGRECORDING", result);
            result_value+=Long.parseLong(result);
        }
        textView_listen_tone.setText("Done");
        isAnalyzing = false;
        detail_done = true;
        temp_freq_value = temp_freq;
//        textView_listen_tone.setText(""+(dataPoints[149].getX()));

    }

    private void stopRecording() {
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

}
