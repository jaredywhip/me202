package com.me202.jaredostdiek.smartbikepart1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ControlActivity extends AppCompatActivity {

    Button unlockButton;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //store the bundle passed from LoginActivity
        Bundle loginBundle=getIntent().getExtras();

        //get the values out by key from bundle
        String username=loginBundle.getString(context.getString(R.string.username));

        //display username as the bike id
        TextView bikeIDTextView = (TextView) findViewById(R.id.bikeID);
        bikeIDTextView.setText(context.getString(R.string.bikeID) + username);

        //display connection status
        TextView connectionState = (TextView) findViewById(R.id.connectionState);
        String connectStatus = context.getString(R.string.notConnected);
        connectionState.setText(context.getString(R.string.status) + connectStatus);

        //create unlock button and set callback
        unlockButton = (Button) findViewById(R.id.unlockButton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get unlock dialog box view
                LayoutInflater li = LayoutInflater.from(context);
                View unlockView = li.inflate(R.layout.unlock_dialog,null);

                //build unlock dialog box
                AlertDialog.Builder unlockDialogBuilder = new AlertDialog.Builder(context);

                //set unlock_dialog.xml to unlockDialogBuilder
                unlockDialogBuilder.setView(unlockView);

                //store input MAC address from dialog box
                final EditText macAdd = (EditText)unlockView.findViewById(R.id.editTextMACAddress);

                //callbacks for dialog box buttons
                unlockDialogBuilder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //make toast of user input MAC address if accept is clicked
                        String macAddStr = macAdd.getText().toString();
                        if (!macAddStr.matches("")) {
                            Toast.makeText(getApplicationContext(), macAddStr, Toast.LENGTH_SHORT).show();
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
    }
}
