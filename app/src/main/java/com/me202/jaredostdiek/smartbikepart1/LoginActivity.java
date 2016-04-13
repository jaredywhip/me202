package com.me202.jaredostdiek.smartbikepart1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control the Login Screen.
 */

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    EditText userInput, passInput;
    Map<String,String > users = new HashMap(); //stores usernames and passwords
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //add user and password to map
        users.put(context.getString(R.string.usersKeyJared), context.getString(R.string.usersValJared));

        //create objects for button and user inputs
        loginButton = (Button)findViewById(R.id.loginButton);
        userInput = (EditText)findViewById(R.id.username);
        passInput = (EditText)findViewById(R.id.password);

        //set login button callback
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //conditional to test if user and password are correct
                String usernameStr = userInput.getText().toString();
                if (users.containsKey(usernameStr) &&
                        passInput.getText().toString().equals(users.get(usernameStr)))
                        {
                            //Create Bundle to pass username to ControlActivity
                            Bundle loginBundle = new Bundle();
                            //assign the values (key, value pairs)
                            loginBundle.putString(context.getString(R.string.username), usernameStr);

                            //create intent for control activity
                            Intent intentControl = new Intent(LoginActivity.this, ControlActivity.class);

                            //assign the bundle to the intent
                            intentControl.putExtras(loginBundle);

                            //launch control activity
                            LoginActivity.this.startActivity(intentControl);
                }
                else {
                    //display toast for incorrect login attempt
                    Toast.makeText(getApplicationContext(), R.string.incorrectLogin, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
