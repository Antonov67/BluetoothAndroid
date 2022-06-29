package com.example.bluetoothandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_ENABLE_CODE = 7;
    private FrameLayout messageFrame;
    private LinearLayout setupFrame;
    private LinearLayout ledFrame;


    private SwitchMaterial btSwitch;
    private SwitchMaterial ledSwitch;

    private Button searchButton;
    private Button disconectButton;

    private ProgressBar progressBar;
    private RecyclerView btDevicesList;

    private BluetoothAdapter bluetoothAdapter;

    private Adapter listAdapter;

    ArrayList<BluetoothDevice> bluetoothDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageFrame = findViewById(R.id.messageFrame);
        setupFrame = findViewById(R.id.setupFrame);
        ledFrame = findViewById(R.id.ledLayout);

        btSwitch = findViewById(R.id.btSwitch);
        ledSwitch = findViewById(R.id.onOffLed);

        progressBar = findViewById(R.id.progressBar);

        searchButton = findViewById(R.id.searchButton);
        disconectButton = findViewById(R.id.disconectButton);

        btDevicesList = findViewById(R.id.recyclerView);
        btDevicesList.setLayoutManager(new LinearLayoutManager(this));
        btDevicesList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        bluetoothDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), " модуль Bluetooth не найден", Toast.LENGTH_LONG).show();
            finish();
        }

        if (bluetoothAdapter.isEnabled()) {
            showSetupFrame();
            btSwitch.setChecked(true);
            setListAdapter(100);
        }

        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        disconectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.equals(btSwitch)){
                    blEnable(isChecked);

                    if (!isChecked){
                        showMessageFrame();
                    }

                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchBtDevice();
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENABLE_CODE) {
            if (resultCode == RESULT_OK && bluetoothAdapter.isEnabled()) {
                showSetupFrame();
                setListAdapter(100);
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

    private void setListAdapter(int type){
        bluetoothDevices.clear();
        switch (type){
            case 100:
                bluetoothDevices = getBoundedBtDevices();
                //Log.d("777", bluetoothDevices.toString());
                listAdapter = new Adapter(bluetoothDevices,this, R.drawable.ic_bluetooth_green);
                break;
            case 200:
                listAdapter = new Adapter(bluetoothDevices,this, R.drawable.ic_bluetooth_red);
                break;
        }

        btDevicesList.setAdapter(listAdapter);
    }

    @SuppressLint("MissingPermission")
    private ArrayList<BluetoothDevice> getBoundedBtDevices(){
       @SuppressLint("MissingPermission") Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> tmpArrayList = new ArrayList<>();
        Log.d("777", deviceSet.size() + "");
        if (deviceSet.size() > 0){
            for (BluetoothDevice device: deviceSet) {
                Log.d("777", device.getName());
                tmpArrayList.add(device);
            }
        }
        return tmpArrayList;
    }

    @SuppressLint("MissingPermission")
    private void startSearchBtDevice(){
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }else {
            bluetoothAdapter.startDiscovery();
        }

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    searchButton.setText("остановить поиск");
                    progressBar.setVisibility(View.VISIBLE);
                    setListAdapter(200);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    searchButton.setText("начать поиск");
                    progressBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null){
                        bluetoothDevices.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    //класс для соединения с блютуз устройством
    private class ConnectThread extends Thread{

        private BluetoothSocket bluetoothSocket = null;
        private boolean isSuccess = false;

        public ConnectThread(BluetoothDevice bluetoothDevice) {
            Method method = null;
            try {
                method = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                bluetoothSocket.connect();
                isSuccess = true;
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Не могу соедениться!", Toast.LENGTH_SHORT).show();
                    }
                });

                cancel();


            }
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}