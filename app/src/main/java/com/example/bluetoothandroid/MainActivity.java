package com.example.bluetoothandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_ENABLE_CODE = 7;
    private FrameLayout messageFrame;
    private LinearLayout setupFrame;

    private SwitchMaterial btSwitch;
    private Button searchButton;
    private ProgressBar progressBar;
    private RecyclerView btDevicesList;

    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageFrame = findViewById(R.id.messageFrame);
        setupFrame = findViewById(R.id.setupFrame);
        btSwitch = findViewById(R.id.btSwitch);
        progressBar = findViewById(R.id.progressBar);
        searchButton = findViewById(R.id.searchButton);
        btDevicesList = findViewById(R.id.recyclerView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), " модуль Bluetooth не найден", Toast.LENGTH_LONG).show();
            finish();
        }

        if (bluetoothAdapter.isEnabled()) {
            showSetupFrame();
        }


        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.equals(btSwitch)){
                    blEnable(isChecked);
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btDevicesList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENABLE_CODE) {
            if (resultCode == RESULT_OK && bluetoothAdapter.isEnabled()) {
                showSetupFrame();
            } else if (resultCode == RESULT_CANCELED) {
                blEnable(true);
            }
        }
    }

    void showMessageFrame() {
        messageFrame.setVisibility(View.VISIBLE);
        setupFrame.setVisibility(View.GONE);
    }

    void showSetupFrame() {
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingPermission")
    private void blEnable(boolean condition) {
        if (condition) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_CODE);
        } else {
            bluetoothAdapter.disable();
        }
    }

}