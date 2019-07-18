package com.example.d2d;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
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

public class Task4 extends AppCompatActivity {

    private EditText editText_input_message;
    private Button button_send_message;
    private TextView textView_sender_status;
    private TextView textView_receiver_status;

    private int recordBufSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task4);
        recordBufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        editText_input_message = (EditText) findViewById(R.id.edittext_Task4_inputmessage);
        button_send_message = (Button) findViewById(R.id.button_Task4_sendmessage);
        textView_sender_status = (TextView) findViewById(R.id.textview_Task4_send_status);
        textView_receiver_status = (TextView) findViewById(R.id.textview_Task4_receiver_status);
        button_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_message();
            }
        });


    }


    private void send_bits(byte[] bytes) {

    }


    private int self_pow(int a, int b) {
        int result = 1;
        for (int i = 0; i < b; i++) {
            result *= a;
        }
        return result;
    }


    private void send_message() {
        if (editText_input_message.getText().length() == 0) {
            Toast.makeText(Task4.this, "The message cannot be empty!", Toast.LENGTH_LONG).show();
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
//        RecordBufSize_Tone_Sender sender = new RecordBufSize_Tone_Sender(recordBufSize, bits, Task4.this);
//        sender.generate_tone();
//        sender.play_sound();


        RecordBufSize_Tone_Sender sender = new RecordBufSize_Tone_Sender(recordBufSize, "hola", Task4.this);
        sender.play_sound();
    }

}
