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
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements  Adapter.ItemClickListener{

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

    protected ConnectThread connectThread;
    private ConnectedThread connectedThread;

    ArrayList<BluetoothDevice> bluetoothDevices;

    private EditText consoleField;

    private ProgressDialog progressDialog;


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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Соединение...");
        progressDialog.setMessage("Ждите...");

        consoleField = findViewById(R.id.consoleField);


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
                ledOnOff(isChecked);
            }
        });





        disconectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedThread != null){
                    connectedThread.cancel();
                }

                if (connectThread != null){
                    connectThread.cancel();
                }

                showSetupFrame();
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

    private void ledOnOff(boolean isChecked){

        if (connectedThread != null && connectThread.isConnect()){
            String command = "";
            command = (isChecked) ? "1" : "0";
            connectedThread.write(command);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
         if (connectedThread != null){
             connectedThread.cancel();
         }
        if (connectThread != null){
            connectThread.cancel();
        }



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
        ledFrame.setVisibility(View.GONE);
    }

    void showSetupFrame() {
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.VISIBLE);
        ledFrame.setVisibility(View.GONE);
    }

    void showLedFrame() {
        ledFrame.setVisibility(View.VISIBLE);
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.GONE);
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

        listAdapter.setClickListener(this);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(View view, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        Log.d("777", "выбрано устройство");
        if (device != null) {
            Log.d("777", device.getName());
            connectThread = new ConnectThread(device);
            connectThread.start();
        }
    }

    //класс для соединения с блютуз устройством
    private class ConnectThread extends Thread{

        private BluetoothSocket bluetoothSocket = null;
        private boolean isSuccess = false;

        public ConnectThread(BluetoothDevice bluetoothDevice) {
            Method method = null;
            try {
                method = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
                progressDialog.show();
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

                progressDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Не могу соедениться!", Toast.LENGTH_SHORT).show();
                    }
                });

                cancel();
            }

            if (isSuccess){

                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLedFrame();
                    }
                });



            }

        }

        public boolean isConnect(){
            return bluetoothSocket.isConnected();
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ConnectedThread extends Thread{

        private final InputStream inputStream;
        private final OutputStream outputStream;

        private boolean isConnected = false;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            StringBuffer buffer = new StringBuffer();
            StringBuffer stringBufferConsole = new StringBuffer();

            while (isConnected){
                try {
                    int bytes = bufferedInputStream.read();
                    buffer.append((char) bytes);
                    int eof = buffer.indexOf("\r\n");
                    if (eof > 0){
                        stringBufferConsole.append(buffer.toString());
                        buffer.delete(0, buffer.length());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                consoleField.setText(stringBufferConsole.toString());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void write(String command){
            byte[] bytes = command.getBytes();
            if (outputStream != null){
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel(){
            try {
                isConnected = false;
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}