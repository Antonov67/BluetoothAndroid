package com.example.bluetoothandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;



import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private FrameLayout messageFrame;
    private LinearLayout setupFrame;

    private SwitchMaterial btSwitch;
    private Button searchButton;
    private ProgressBar progressBar;
    private RecyclerView btDevicesList;


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


        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
    void showMessageFrame(){
        messageFrame.setVisibility(View.VISIBLE);
        setupFrame.setVisibility(View.GONE);
    }

    void showSetupFrame(){
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.VISIBLE);
    }

}