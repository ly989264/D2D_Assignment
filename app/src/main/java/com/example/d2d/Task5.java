package com.example.d2d;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Task5 extends AppCompatActivity {

    private TextView textView_params;
    private Button button_reset_params;
    private EditText editText_input_message;
    private Button button_send_message;
    private TextView textView_sender_status;
    private TextView textView_receiver_status;
    private Button button_start_receive;
    private Button button_stop_receive;
    private Button button_show_result;

    private int recordBufSize;
    private AudioRecord recorder;
    private boolean isRecording;
    private Thread recordingThread;
    private double duration_fromAlert;
    private int container_size_fromAlert;
    private int sleep_time_fromAlert;

    private long threshold;

    private boolean wrong = false;

    private ArrayList<Task5.my_longs_5> intermediate_data = new ArrayList<>();
    private ArrayList<Integer> res = new ArrayList<>();
    private ArrayList<Integer> second_res = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task5);
        recordBufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        textView_params = (TextView) findViewById(R.id.textview_Task5_params);
        button_reset_params = (Button) findViewById(R.id.button_Task5_reset_params);
        editText_input_message = (EditText) findViewById(R.id.edittext_Task5_inputmessage);
        button_send_message = (Button) findViewById(R.id.button_Task5_sendmessage);
        textView_sender_status = (TextView) findViewById(R.id.textview_Task5_send_status);
        textView_receiver_status = (TextView) findViewById(R.id.textview_Task5_receiver_status);
        button_start_receive = (Button) findViewById(R.id.button_Task5_receiver);
        button_stop_receive = (Button) findViewById(R.id.button_Task5_stop_receive);
        button_show_result = (Button) findViewById(R.id.button_Task5_show_result);
        button_reset_params.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_params();
            }
        });
        button_show_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intermediate_data_process();
            }
        });
        button_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_message();
            }
        });
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                recordBufSize);
        button_start_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receive();
            }
        });
        button_stop_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopReceive();
            }
        });
        set_params();
    }


    private void set_params() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Task5.this);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle("Choose Data Transfer Parameter");
        alertBuilder.setMessage("Which data transfer duration for each bit you want to use?");
        alertBuilder.setPositiveButton("0.3s", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                duration_fromAlert = 0.3;
                container_size_fromAlert = 4410;
                sleep_time_fromAlert = 360;
                textView_params.setText("Duration: 0.3s");
            }
        });
        alertBuilder.setNegativeButton("0.09s", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                duration_fromAlert = 0.09;
                container_size_fromAlert = 1323;
                sleep_time_fromAlert = 125;
                textView_params.setText("Duration: 0.09s");
            }
        });
        alertBuilder.show();
    }


    private int self_pow(int a, int b) {
        int result = 1;
        for (int i = 0; i < b; i++) {
            result *= a;
        }
        return result;
    }

    private int get_hash_message(String message) {
        int hash = 7;
        for (int index = 0; index < message.length(); index++) {
            hash = hash * 31 + (int) message.charAt(index);
        }
        hash = hash % 127;
        return hash + 128;
    }

    private void send_message() {
        if (editText_input_message.getText().length() == 0) {
            Toast.makeText(Task5.this, "The message cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }
        String message = editText_input_message.getText().toString();
        editText_input_message.setText("");
        // get the ASCII code of the message
//        boolean[] bits = new boolean[message.length()*8];
//        int temp_value = 0;
//        int current_bit_value = 0;
//        for (int i = 0; i < message.length(); i++) {
//            temp_value = (int) message.charAt(i);
//            for (int j = 0; j<8; j++) {
//                if (temp_value >= self_pow(2, 7-j)) {
//                    current_bit_value = 1;
//                    temp_value -= self_pow(2, 7-j);
//                } else {
//                    current_bit_value = 0;
//                }
//                bits[8*i+j] = current_bit_value == 1;
//            }
//        }
//        String result = "";
//        for (boolean each : bits) {
//            result += (each?"1":"0");
//        }
//        Log.d("ASCIIOFMESSAGE", result);
//        RecordBufSize_Tone_Sender sender = new RecordBufSize_Tone_Sender(recordBufSize, bits, Task5.this);
//        sender.generate_tone();
//        sender.play_sound();


//        RecordBufSize_Tone_Sender sender = new RecordBufSize_Tone_Sender(recordBufSize*2, message, Task5.this);
//        sender.play_sound();
        final String message_copy = message;
        textView_sender_status.setText("Sending: "+message);
        Thread send_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Certain_Time_Tone_Sender_T5 sender = new Certain_Time_Tone_Sender_T5(duration_fromAlert, message_copy, Task5.this);
                sender.play_sound();
            }
        });
        send_thread.start();
        Thread setting_timer = new Thread(new Runnable() {
            @Override
            public void run() {
                int time_to_sleep = (message_copy.length() + 1) * 4 * 2 * sleep_time_fromAlert + 4 * sleep_time_fromAlert;
                try {
                    TimeUnit.MILLISECONDS.sleep(time_to_sleep);
                } catch (Exception e) {
                    Log.d("CANNOTSLEEP", "Cannot sleep in setting_timer thread");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView_sender_status.setText("Done");
                    }
                });
            }
        });
        setting_timer.start();
    }

    private class my_shorts_5 {
        // what "shorts" means is an array of short, not clothes...
        short[] shorts;
        Context context;

        my_shorts_5(int recordBufSize, short[] shorts, Context context) {
            this.shorts = new short[recordBufSize];
            this.shorts = shorts;
            this.context = context;
        }
    }

    private class my_longs_5 {
        long clearh;
        long clearl;
        long high;
        long low;
        long high2;
        long low2;

        my_longs_5(long a, long b, long c, long d, long e, long f) {
            this.clearh = a;
            this.clearl = b;
            this.high = c;
            this.low = d;
            this.high2 = e;
            this.low2 = f;
        }

        public int generator() {
            if (clearh > clearl && clearh > high && clearh > low && clearh > high2 && clearh > low2) {
                return 0;  // clearh
            } else if (clearl > clearh && clearl > high && clearl > low && clearl > high2 && clearl > low2) {
                return 1;  // clearl
            } else if (high > clearh && high > clearl && high > low && high > high2 && high > low2) {
                return 2;  // high
            } else if (low > clearh && low > clearl && low > high && low > high2 && low > low2) {
                return 3;  // low
            } else if (high2 > clearh && high2 > clearl && high2 > high && high2 > low && high2 > low2) {
                return 4;  // high2
            } else {
                return 5;  // low2
            }
        }

        public boolean check_noise() {
            if (clearh > threshold || clearl > threshold || high > threshold || low > threshold || high2 > threshold || low2 > threshold) {
                return true;
            }
            return false;
        }
    }

    private void receive() {
        Log.d("Task5STARTRECEIVE", "Start receive");
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

    private void writeAudioDataToFile() {
        intermediate_data.clear();
        final short sData[] = new short[container_size_fromAlert];
        isRecording = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_receiver_status.setText("Receiving started");
            }
        });
        while (isRecording) {
            recorder.read(sData, 0, container_size_fromAlert);
            Task5.my_shorts_5 current_shorts = new Task5.my_shorts_5(container_size_fromAlert, sData, Task5.this);
            new Task5.asyncOperate().execute(current_shorts);

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_receiver_status.setText("Receiving stopped");
            }
        });
    }

    private void stopReceive() {
        isRecording = false;
        recorder.stop();
    }


    private class async_result {
        boolean pos;// true means the first one, false means the second one
        boolean isData;// if there is data in this page
        boolean result_data;// the data contained in this page

        async_result(boolean pos, boolean isData, boolean result_data) {
            this.pos = pos;
            this.isData = isData;
            this.result_data = result_data;
        }
    }

    private int compare_data(long result_clearh, long result_clearl, long result_high, long result_low) {
        long a1 = Math.max(result_clearh, result_clearl);
        return 1;
    }

    private void intermediate_data_process() {
        res.clear();
        long temp_long = 0;
        for (int i = 0; i < 40; i++) {
            if (intermediate_data.get(i).clearh > temp_long) {
                temp_long = intermediate_data.get(i).clearh;
            }
        }
        threshold = (long) (temp_long * 0.03);
        for (Task5.my_longs_5 each : intermediate_data) {

            //************************Writing test
            String str = "";
            str+=each.clearh;
            str+=" ";
            str+=each.clearl;
            str+=" ";
            str+=each.high;
            str+=" ";
            str+=each.low;
            str+=" ";
            str+=each.high2;
            str+=" ";
            str+=each.low2;

            //        // collect some data for analysis
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = openFileOutput("data8.txt", Context.MODE_APPEND);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(str);
                writer.write("\n");
                writer.close();
            } catch (Exception e) {
                Log.d("ASCIIASCII", "Wrong exporting");
            }
            //************************Writing test

            if (each.check_noise()) {
                res.add(each.generator());
            } else {
                res.add(-1);
            }
        }
        //************************Writing test
        String str_2 = "";
        for(int index = 0; index < res.size(); index++) {
            str_2 += res.get(index);
            str_2 += " ";
        }
        FileOutputStream out2 = null;
        BufferedWriter writer2 = null;
        try {
            out2 = openFileOutput("data10.txt", Context.MODE_APPEND);
            writer2 = new BufferedWriter(new OutputStreamWriter(out2));
            writer2.write(str_2);
            writer2.write("\n");
            writer2.close();
        } catch (Exception e) {
            Log.d("ASCIIASCII", "Wrong exporting");
        }
        //************************Writing test
        //*************************Plan A
//        int cnt = 0;
//        int prev_pos = -1;
//        boolean clear_flag = false;
//        second_res.clear();
//        for (int current_index = 0; current_index < res.size(); current_index++) {
//            if (! clear_flag) {
//                if (res.get(current_index) == -1) {
//                    continue;
//                }
//                if (res.get(current_index) == 0) {
//                    clear_flag = true;
//                    cnt++;
//                    prev_pos = 0;
//                    continue;
//                }
//                continue;
//            }
//            if (res.get(current_index) == prev_pos) {
//                cnt++;
//            } else if (res.get(current_index) == -1) {
//                if (cnt != 0) {
//                    second_res.add(prev_pos);
//                    cnt = 0;
//                }
//            } else {
//                if (cnt > 1) {
//                    second_res.add(prev_pos);
//                }
//                prev_pos = res.get(current_index);
//                cnt = 0;
//            }
//        }
        //*************************Plan A

        // need to test the Plan A and Plan B
        // Plan B need to solve 223333 problem

        //*************************Plan B
        int cnt = 0;
        int prev_pos = -5;
        boolean clear_flag = false;
        second_res.clear();
        // modified
        for (int current_index = 0; current_index < res.size(); current_index++) {
            if (prev_pos == -5 && res.get(current_index) != -1) {
                prev_pos = res.get(current_index);
                cnt = 1;
            }
            else if (res.get(current_index) == prev_pos) {
                if (cnt > 3) {
                    second_res.add(prev_pos);
                    cnt = 1;
                } else {
                    cnt++;
                }
            } else if (res.get(current_index) == -1) {
                if (cnt != 0) {
                    second_res.add(prev_pos);
                    prev_pos = -5;
                    cnt = 0;
                }
            } else {
                if (cnt > 1) {
                    second_res.add(prev_pos);
                }
                cnt = 1;
                prev_pos = res.get(current_index);
            }
        }
//        Plan B has potential problem of situation such as 223333
        //22333 222333
        // fixed
        //*********************Plan B

        int temp_from_sec = 0;
        int temp_sec_cnt = 0;
        cnt = 0;
        String string = "";
        ArrayList<Integer> bytes = new ArrayList<>();
        int temp = 0;
        for (int each : second_res) {
            if (each == 0 || each == 1) {
                continue;
            }
            Log.d("holahola", ""+each);
            temp_sec_cnt++;
            if (each == 2) {
                temp_from_sec += self_pow(2, (3 - temp_sec_cnt)*2-1);
                temp_from_sec += self_pow(2, (3 - temp_sec_cnt)*2-2);
            } else if (each == 4) {
                temp_from_sec += self_pow(2, (3 - temp_sec_cnt)*2-1);
            } else if (each == 5) {
                temp_from_sec += self_pow(2, (3 - temp_sec_cnt)*2-2);
            }
            if (temp_sec_cnt == 2) {
                temp_sec_cnt = 0;
                cnt++;
                cnt++;
                switch (temp_from_sec) {
                    case 0:
                        bytes.add(0);
                        bytes.add(0);
                        break;
                    case 3:
                        bytes.add(0);
                        bytes.add(1);
                        break;
                    case 5:
                        bytes.add(1);
                        bytes.add(0);
                        break;
                    case 6:
                        bytes.add(1);
                        bytes.add(1);
                        break;
                    case 8:
                        bytes.add(0);
                        bytes.add(0);
                        break;
                    case 11:
                        bytes.add(0);
                        bytes.add(1);
                        break;
                    case 13:
                        bytes.add(1);
                        bytes.add(0);
                        break;
                    case 14:
                        bytes.add(1);
                        bytes.add(1);
                        break;
                        default:
                            wrong = true;
                            break;
                }
                temp_from_sec = 0;
            }
            if (wrong) {
                break;
            }
            if (cnt == 8) {
                temp = 0;
                for (int i = 0; i < 8; i++) {
                    temp += (bytes.get(i) * self_pow(2, 7-i));
                }
                bytes.clear();
                string += Character.toString((char)temp);
                cnt = 0;
            }
        }
        String final_result = null;
        String hash_string = null;
        if (wrong) {
            textView_receiver_status.setText("Potential error occurs detected from Hamming distance");
            return;
        }
        if (string.length() > 1) {
            final_result = string.substring(0, string.length()-1);
            hash_string = string.substring(string.length()-1);
            int hash_value = hash_string.charAt(0);
//            Log.d("HASHINGHASHING", ""+hash_string);
            Log.d("HASHINGHASHING", "final_string: "+final_result);
            Log.d("HASHINGHASHING", "hash_value: "+hash_value);
            Log.d("HASHINGHASHING", "calculated: "+get_hash_message(final_result));
            if (get_hash_message(final_result) == hash_value) {
                textView_receiver_status.setText(final_result);
            } else {
                textView_receiver_status.setText("Not transmitted successfully "+hash_value);
            }
        } else {
            textView_receiver_status.setText("Not transmitted successfully, c1");
        }
//        textView_receiver_status.setText(string);
    }

    class asyncOperate extends AsyncTask<Task5.my_shorts_5, Void, Task5.my_longs_5> {

        private double[] freqs = {1000, 400, 3000};

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Task5.my_longs_5 doInBackground(Task5.my_shorts_5... my_shorts) {
            Parse_freq parser = new Parse_freq(44100, 19000, 552, container_size_fromAlert, my_shorts[0].shorts);
            long result_clearh = Long.parseLong(parser.analyse());
            parser.set_params(18500);
            long result_clearl = Long.parseLong(parser.analyse());
            parser.set_params(18000);
            long result_high = Long.parseLong(parser.analyse());
            parser.set_params(17500);
            long result_low = Long.parseLong(parser.analyse());
            parser.set_params(17000);
            long result_high2 = Long.parseLong(parser.analyse());
            parser.set_params(16500);
            long result_low2 = Long.parseLong(parser.analyse());

//            if (result_clearh > result_high || result_clearl > )

            String str = "";
            str+=result_clearh;
            str+=" ";
            str+=result_clearl;
            str+=" ";
            str+=result_high;
            str+=" ";
            str+=result_low;
            str+=" ";
            str+=result_high2;
            str+=" ";
            str+=result_low2;
            Log.d("BACKGROUNDRESULT", str);


            //        // collect some data for analysis
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = my_shorts[0].context.openFileOutput("data9.txt", Context.MODE_APPEND);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(str);
                writer.write("\n");
                writer.close();
            } catch (Exception e) {
                Log.d("ASCIIASCII", "Wrong exporting");
            }

            return new Task5.my_longs_5(result_clearh, result_clearl, result_high, result_low, result_high2, result_low2);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Task5.my_longs_5 my_longs) {
            super.onPostExecute(my_longs);
            intermediate_data.add(my_longs);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    textView_sender_status.setText(""+intermediate_data.size());
//                }
//            });
        }

    }
}
