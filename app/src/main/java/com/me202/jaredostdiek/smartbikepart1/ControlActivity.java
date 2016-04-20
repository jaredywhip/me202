package com.me202.jaredostdiek.smartbikepart1;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.UUID;
import java.util.logging.Handler;

/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control the Control Screen.adfd
 */

public class ControlActivity extends AppCompatActivity {

    Button unlockButton, historyButton, lightStateButton, lightModeButton;
    Context context = this;
    private final static int REQUEST_ENABLE_BT = 1; //used for enable BLE popup
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback mGattCallBack;
    private BluetoothGatt mBluetoothGatt;
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    //My UUID public static UUID UART_UUID = UUID.fromString("00001530-1212-EFDE-1523-785FEABCD123");


    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static String bluefruitMacAddr = "F0:E5:B2:9B:91:A5";
    private Boolean autoConnectBoolean = false;
    TextView connectionState, autoTextView, onTextView, solidTextView, blinkingTextView;
    private Boolean deviceFound = false;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //set device boolean false
        deviceFound = false;

        //store the bundle passed from LoginActivity
        Bundle loginBundle = getIntent().getExtras();

        //get the values out by key from bundle
        String username = loginBundle.getString(context.getString(R.string.username));

        //display username as the bike id
        TextView bikeIDTextView = (TextView) findViewById(R.id.bikeID);
        bikeIDTextView.setText(context.getString(R.string.bikeID) + username);

        //disable toggle buttons
        enableToggleButtons(false);

        mGattCallBack = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.i("onConnectionStateChange", "Status: " + status);


//                switch (newState) {
//                    case BluetoothProfile.STATE_CONNECTED:
//                        Log.i("gattCallback", "STATE_CONNECTED");
//                        String connectStatus = context.getString(R.string.connected);
//                        connectionState.setText(context.getString(R.string.status) + connectStatus);
//                        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                        gatt.discoverServices();
//                        break;
//                    case BluetoothProfile.STATE_DISCONNECTED:
//                        Log.e("gattCallback", "STATE_DISCONNECTED");
//                        break;
//                    default:
//                        Log.e("gattCallback", "STATE_OTHER");
//                }


                if (newState == BluetoothGatt.STATE_CONNECTED){
                    if(status == BluetoothGatt.GATT_SUCCESS) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //stuff that updates ui
                                enableToggleButtons(true);
                            }
                        });

                        gatt.discoverServices();

                        //discover services. use uuids for rx and tx
                        if (!gatt.discoverServices()) {Toast.makeText(context, "Didn't discover services", Toast.LENGTH_SHORT).show();}

                    }

                    }
                else if (newState == BluetoothGatt.STATE_DISCONNECTED){

                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.i("onServicesDiscovered", "Status: " + status);
                // Notify connection failure if service discovery failed
                if (status == BluetoothGatt.GATT_FAILURE) {return;}
                // Save reference to each UART characteristic, module level
                BluetoothGattCharacteristic tx =
                        gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                if (!gatt.setCharacteristicNotification(tx, true)) {
                    return;}
                BluetoothGattDescriptor desc =
                        tx.getDescriptor(CLIENT_UUID);
                if (desc == null) {return;}
                desc.
                        setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {return;}
                // Success!

            }
        };

        //scan callback method
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Toast.makeText(context, device.getAddress().toString(), Toast.LENGTH_SHORT).show();
                if (device.getAddress().equals(bluefruitMacAddr)) {
                    //stop scanning devices
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    //set MAC address as bike id
                    TextView bikeIDTextView = (TextView) findViewById(R.id.bikeID);
                    bikeIDTextView.setText(context.getString(R.string.bikeID) + device.getAddress().toString());

                    Toast.makeText(context, "Found it!", Toast.LENGTH_SHORT).show();
                    deviceFound = true;
                    mBluetoothGatt = device.connectGatt(context, autoConnectBoolean, mGattCallBack);

                }
            }

        };

        //create unlock button and set callback
        unlockButton = (Button) findViewById(R.id.unlockButton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                        //make toast of user input MAC address if accept is clicked
                        String macAddStr = macAdd.getText().toString();
                        if (!macAddStr.matches("")) {
                            //overright mac address with user input
                            bluefruitMacAddr = macAdd.getText().toString();
                            //Toast.makeText(getApplicationContext(), macAddStr, Toast.LENGTH_SHORT).show();

                            // Initializes Bluetooth adapter.
                            final BluetoothManager bluetoothManager =
                                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            mBluetoothAdapter = bluetoothManager.getAdapter();

                            //Set up bluetooth
                            //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


                            if (mBluetoothAdapter == null) {
                                // Device does not support Bluetooth
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



                            //if (deviceFound == false) {
                            mBluetoothAdapter.startLeScan(mLeScanCallback);
                            //} else {
                            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            //}


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
            }
        });




        //create unlock button and set callback
        historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //temp code test for send data
                tx.setValue(byte[] data);
                writingFlag = true;
                Gatt.writeCharacteristic(tx);



                //create intent for control activity
                Intent intentHistory = new Intent(ControlActivity.this, RideHistoryActivity.class);
                //launch ride history activity
                ControlActivity.this.startActivity(intentHistory);
            }

        });

        lightModeButton = (Button) findViewById(R.id.toggleLightMode);
        lightModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }

    public void enableToggleButtons(boolean select){

        if (select == true) {
            //display connection status
            connectionState = (TextView) findViewById(R.id.connectionState);
            String connectStatus = context.getString(R.string.connected);
            connectionState.setText(context.getString(R.string.status) + connectStatus);

            //set toggle button visibility
            lightModeButton = (ToggleButton) findViewById(R.id.toggleLightMode);
            lightStateButton = (ToggleButton) findViewById(R.id.toggleLightState);
            lightStateButton.getBackground().setAlpha(255); // make transparent
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
            lightStateButton.getBackground().setAlpha(50); // make transparent
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

}



