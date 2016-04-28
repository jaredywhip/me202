package com.me202.jaredostdiek.smartbikepart1;

//android imports
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
//java imports
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control the Control Screen. Also includes BLE
 * connection functions.
 */

public class ControlActivity extends AppCompatActivity {

    //initialize view elements
    private Button unlockButton, historyButton;
    private ToggleButton lightStateButton, lightModeButton;
    private TextView connectionState, autoTextView, onTextView, solidTextView, blinkingTextView;
    Context context = this;

    //BLE variables
    private final static int REQUEST_ENABLE_BT = 1; //used for enable BLE popup
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback mGattCallBack;
    private BluetoothGatt mBluetoothGatt;
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    public static String bluefruitMacAddr = "F0:E5:B2:9B:91:A5";
    private Boolean autoConnectBoolean = false;
    private boolean isConnected = false;

    //Communication Protocol
    private byte lightModeByte = 00; //default solid
    private byte lightStateByte = 00; //default auto
    private static byte startByte = 99;
    private static byte endByte = 77;
    private boolean writingFlag;
    Timer timerSendData;
    TimerTask timerTaskSendData;

    //recieved moving and accel data
    private double accelData;
    private boolean moving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //store the bundle passed from LoginActivity
        Bundle loginBundle = getIntent().getExtras();

        //get the values out by key from bundle
        String username = loginBundle.getString(context.getString(R.string.username));

        //display username as the bike id while not connected
        TextView bikeIDTextView = (TextView) findViewById(R.id.bikeID);
        bikeIDTextView.setText(context.getString(R.string.bikeID) + username);

        //disable toggle buttons while not connected
        enableToggleButtons(false);

        mGattCallBack = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.i("onConnectionStateChange", "Status: " + status);

                //check if connected
                if (newState == BluetoothGatt.STATE_CONNECTED){
                    if(status == BluetoothGatt.GATT_SUCCESS) {

                        //discover BLE services
                        gatt.discoverServices();

                        //discover services.
                        if (!gatt.discoverServices()) {Toast.makeText(context, "Didn't discover services", Toast.LENGTH_SHORT).show();}
                        }
                    }
                else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                    //update view if disconnected
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //update UI
                            enableToggleButtons(false);
                            isConnected = false;
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.i("onServicesDiscovered", "Status: " + status);

                //update view
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui
                        enableToggleButtons(true);
                        isConnected = true;
                        unlockButton.setText(R.string.lock);
                        unlockButton.setBackgroundColor(getResources().getColor(R.color.black));
                    }
                });

                // Notify connection failure if service discovery failed
                if (status == BluetoothGatt.GATT_FAILURE) {return;}

                //define tx
                tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                if (!gatt.setCharacteristicNotification(tx, true)) {
                    return;}
                BluetoothGattDescriptor txDesc = tx.getDescriptor(CLIENT_UUID);

                //define rx
                rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
                if (!gatt.setCharacteristicNotification(rx, true)) {
                    return;}
                BluetoothGattDescriptor rxDesc = rx.getDescriptor(CLIENT_UUID);

                rxDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(rxDesc)) {return;}
                // Success!

                //set timer for data heartbeat
                timerSendData = new Timer();

                //set up timerTask
                initializeTimerTask();

                //schedule data send
                timerSendData.schedule(timerTaskSendData, 1000, 1000);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        //error handling
                        Log.d("Error writing", "characteristic: " + characteristic);
                    }
                    writingFlag = false;
                }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                String movingPacket = characteristic.getStringValue(0);
                Log.i("onCharacteristicChanged", movingPacket);

                //get moving and accel data and parse the string
                String[] separatedMovingPacket = movingPacket.split(":");
                if (separatedMovingPacket[0].equals("1")){
                    moving = true;
                } else {
                    moving = false;
                }
                //moving = Boolean.valueOf(separatedMovingPacket[0]);
                accelData = Double.parseDouble(separatedMovingPacket[1]);
                Log.i("moving", separatedMovingPacket[0]);
                Log.i("accelData", separatedMovingPacket[1]);

                //respond if moving
                if (moving) {

                    //vibrate the phone
                    Log.i("inif", separatedMovingPacket[0]);
                    Vibrator vib = (Vibrator) getSystemService(context.VIBRATOR_SERVICE);
                    vib.vibrate(200);
                    if (vib.hasVibrator()){
                        vib.vibrate(200);
                    }

                    //update view
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui
                            Log.i("updatebackcolor", "word");
                            RelativeLayout controlLayout = (RelativeLayout) findViewById(R.id.controlLayout);
                            controlLayout.setBackgroundColor(getResources().getColor(R.color.ligthGray));
                        }
                    });

                } else {
                    Log.i("inelse", separatedMovingPacket[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui
                            RelativeLayout controlLayout = (RelativeLayout) findViewById(R.id.controlLayout);
                            controlLayout.setBackgroundColor(getResources().getColor(R.color.whitesmoke));
                        }
                    });
                }
            }
        };

        //scan callback method
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                //find make address for bluefruit
                if (device.getAddress().equals(bluefruitMacAddr)) {

                    //stop scanning devices
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    //set MAC address as bike id
                    TextView bikeIDTextView = (TextView) findViewById(R.id.bikeID);
                    bikeIDTextView.setText(context.getString(R.string.bikeID) + device.getAddress().toString());

                    Toast.makeText(context, "Found Bluefruit", Toast.LENGTH_SHORT).show();

                    //connnect gatt
                    mBluetoothGatt = device.connectGatt(context, autoConnectBoolean, mGattCallBack);
                }
            }
        };

        //create unlock button and set callback
        unlockButton = (Button) findViewById(R.id.unlockButton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnected == false) {

                    //get unlock dialog box view
                    LayoutInflater li = LayoutInflater.from(context);
                    View unlockView = li.inflate(R.layout.unlock_dialog, null);

                    //build unlock dialog box
                    AlertDialog.Builder unlockDialogBuilder = new AlertDialog.Builder(context);

                    //set unlock_dialog.xml to unlockDialogBuilder
                    unlockDialogBuilder.setView(unlockView);

                    //store input MAC address from dialog box
                    final EditText macAdd = (EditText) unlockView.findViewById(R.id.editTextMACAddress);

                    //prefill edittext with bluefruit mac address
                    macAdd.setText(bluefruitMacAddr);

                    //callbacks for dialog box buttons
                    unlockDialogBuilder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String macAddStr = macAdd.getText().toString();

                            if (!macAddStr.matches("")) {
                                //overwrite mac address with user input
                                bluefruitMacAddr = macAdd.getText().toString();

                                // Initializes Bluetooth adapter.
                                final BluetoothManager bluetoothManager =
                                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                                mBluetoothAdapter = bluetoothManager.getAdapter();

                                if (mBluetoothAdapter == null) {
                                    // Device does not support Bluetooth
                                    Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                                }

                                //make sure bluetooth is on
                                if (!mBluetoothAdapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                }

                                //check if phone has le enabled
                                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                    Toast.makeText(context, "No LE Support.", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }

                                //scan for devices
                                mBluetoothAdapter.startLeScan(mLeScanCallback);
                                dialog.cancel();
                            }
                        }
                    });

                    unlockDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    // create dialog box
                    AlertDialog unlockDialog = unlockDialogBuilder.create();
                    // Show dialog
                    unlockDialog.show();
                } else {
                    disconnect();
                }
            }

        });

        //send information from toggle buttons to arduino via BLE
        lightStateButton = (ToggleButton) findViewById(R.id.toggleLightState);
        lightStateButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lightStateButton.isChecked()) {
                    lightStateByte = 01; //on
                    byte[] data = {startByte, lightStateByte, lightModeByte, endByte};
                    tx.setValue(data);
                    writingFlag = true;
                    mBluetoothGatt.writeCharacteristic(tx);
                } else {
                    lightStateByte = 00; //auto
                    byte[] data = {startByte, lightStateByte, lightModeByte, endByte};
                    tx.setValue(data);
                    writingFlag = true;
                    mBluetoothGatt.writeCharacteristic(tx);
                }
            }
        });

        lightModeButton = (ToggleButton) findViewById(R.id.toggleLightMode);
        lightModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lightModeButton.isChecked()) {
                    lightModeByte = 01; //blinking
                    byte[] data = {startByte, lightStateByte, lightModeByte, endByte};
                    tx.setValue(data);
                    writingFlag = true;
                    mBluetoothGatt.writeCharacteristic(tx);
                } else {
                    lightModeByte = 00; //solid
                    byte[] data = {startByte, lightStateByte, lightModeByte, endByte};
                    tx.setValue(data);
                    writingFlag = true;
                    mBluetoothGatt.writeCharacteristic(tx);
                }
            }
        });

        //create unlock button and set callback
        historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create intent for control activity
                Intent intentHistory = new Intent(ControlActivity.this, RideHistoryActivity.class);
                //launch ride history activity
                ControlActivity.this.startActivity(intentHistory);
            }
        });
    }

    public void initializeTimerTask() {
        timerTaskSendData = new TimerTask() {
            @Override
            public void run() {
                //send data to ble
                byte[] data = {startByte, lightStateByte, lightModeByte, endByte};
                tx.setValue(data);
                writingFlag = true;
                mBluetoothGatt.writeCharacteristic(tx);
            }
        };
    }

    //method to enable/disable buttons depending on connection state
    public void enableToggleButtons(boolean select){

        if (select == true) {
            //display connection status
            connectionState = (TextView) findViewById(R.id.connectionState);
            String connectStatus = context.getString(R.string.connected);
            connectionState.setText(context.getString(R.string.status) + connectStatus);

            //set toggle button visibility
            lightModeButton = (ToggleButton) findViewById(R.id.toggleLightMode);
            lightStateButton = (ToggleButton) findViewById(R.id.toggleLightState);
            lightStateButton.getBackground().setAlpha(255); // change opacity
            lightModeButton.getBackground().setAlpha(255);
            lightModeButton.setEnabled(true);
            lightStateButton.setEnabled(true);

            //set text visibility
            autoTextView = (TextView) findViewById(R.id.autoTextView);
            onTextView = (TextView) findViewById(R.id.onTextView);
            solidTextView = (TextView) findViewById(R.id.solidTextView);
            blinkingTextView = (TextView) findViewById(R.id.blinkingTextView);
            autoTextView.setTextColor(getResources().getColor(R.color.darkgray));
            onTextView.setTextColor(getResources().getColor(R.color.darkgray));
            solidTextView.setTextColor(getResources().getColor(R.color.darkgray));
            blinkingTextView.setTextColor(getResources().getColor(R.color.darkgray));
        }
        else{
            //display connection status
            connectionState = (TextView) findViewById(R.id.connectionState);
            String connectStatus = context.getString(R.string.notConnected);
            connectionState.setText(context.getString(R.string.status) + connectStatus);

            //set toggle button visibility
            lightModeButton = (ToggleButton) findViewById(R.id.toggleLightMode);
            lightStateButton = (ToggleButton) findViewById(R.id.toggleLightState);
            lightStateButton.getBackground().setAlpha(50); // change opacity
            lightModeButton.getBackground().setAlpha(50);
            lightModeButton.setEnabled(false);
            lightStateButton.setEnabled(false);

            //set text visibility
            autoTextView = (TextView) findViewById(R.id.autoTextView);
            onTextView = (TextView) findViewById(R.id.onTextView);
            solidTextView = (TextView) findViewById(R.id.solidTextView);
            blinkingTextView = (TextView) findViewById(R.id.blinkingTextView);
            autoTextView.setTextColor(getResources().getColor(R.color.ligthGray));
            onTextView.setTextColor(getResources().getColor(R.color.ligthGray));
            solidTextView.setTextColor(getResources().getColor(R.color.ligthGray));
            blinkingTextView.setTextColor(getResources().getColor(R.color.ligthGray));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //disconnect BLE with app is destoryed
        disconnectDestroy();
    }

    public void disconnectDestroy() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        mBluetoothGatt = null;
        tx = null;
        rx = null;
        isConnected = false;
    }

    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        //update boolean
        isConnected = false;

        //update button
        unlockButton.setText(R.string.unlock);
        unlockButton.setBackgroundColor(getResources().getColor(R.color.darkgray));
    }
}