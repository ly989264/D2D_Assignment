package com.example.d2d;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // the MainActivity is the wrapper activity, which calls other activities
    private Button button_task1;
    private Button button_task2;
    private Button button_task3;
    private Button button_task4;
    private Button button_task5;
    private Button button_t4;
    private Button button_t5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_task1 = (Button) findViewById(R.id.button_task1);
        button_task2 = (Button) findViewById(R.id.button_task2);
        button_task3 = (Button) findViewById(R.id.button_task3);
        button_task4 = (Button) findViewById(R.id.button_task4);
        button_task5 = (Button) findViewById(R.id.button_task5);
        button_t4 = (Button) findViewById(R.id.button_task4_BFSK);
        button_t5 = (Button) findViewById(R.id.button_task5_BFSK);
        button_task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Task1.class);
                startActivity(intent);
            }
        });
        button_task2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task2.class);
                startActivity(intent);
            }
        });
        button_task3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task3.class);
                startActivity(intent);
            }
        });
        button_task4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task4.class);
                startActivity(intent);
            }
        });
        button_task5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task5.class);
                startActivity(intent);
            }
        });
        button_t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task4_BFSK.class);
                startActivity(intent);
            }
        });
        button_t5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Task5_BFSK.class);
                startActivity(intent);
            }
        });
    }

}
