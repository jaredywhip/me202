package com.me202.jaredostdiek.smartbikepart1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Query;


/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control the Login Screen.
 */

public class LoginActivity extends AppCompatActivity {

    Button loginButton, nextButton, registerButton;
    EditText userInput, passInput;
    //Map<String,String > users = new HashMap(); //stores usernames and passwords
    Context context = this;
    String usernameStr;
    Bundle loginBundle;
    Firebase myFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //create firebase reference
        myFirebaseRef = new Firebase("https://vivid-heat-8090.firebaseio.com/");

        //add user and password to map
        //users.put(context.getString(R.string.usersKeyJared), context.getString(R.string.usersValJared));

        //create objects for button and user inputs
        loginButton = (Button)findViewById(R.id.loginButton);
        userInput = (EditText)findViewById(R.id.username);
        passInput = (EditText)findViewById(R.id.password);
        nextButton = (Button) findViewById(R.id.nextButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameStr = userInput.getText().toString();

                //test if user email exists
                myFirebaseRef.createUser(usernameStr, "test", new Firebase.ValueResultHandler<Map<String, Object>>() {


                    @Override
                    public void onError(FirebaseError firebaseError) {

                        System.out.println("Email taken error coe: " + FirebaseError.EMAIL_TAKEN);
                        System.out.println("error: " + FirebaseError.INVALID_EMAIL);
                        System.out.println("error: " + firebaseError.getCode());

                        //hack line to check if the user email is alread taken assume they are registered
                        if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
                            //already registered case
                            passInput.setVisibility(View.VISIBLE);
                            passInput.setHint(R.string.passwordHint);
                            loginButton.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.GONE);
                        }
                        else {
                            //have user register
                            Toast.makeText(getApplicationContext(), R.string.registerToast, Toast.LENGTH_LONG).show();
                            passInput.setVisibility(View.VISIBLE);
                            passInput.setHint(R.string.setPasswordHint);
                            registerButton.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onSuccess(Map<String, Object> stringObjectMap) {

                    }
                });

//                //test if user email exists
//                myFirebaseRef.createUser(usernameStr, "test", new Firebase.ValueResultHandler<Map<String, Object>>() {
//
//
//                    @Override
//                    public void onError(FirebaseError firebaseError) {
//
//                        System.out.println("Email taken error coe: " + FirebaseError.EMAIL_TAKEN);
//                        System.out.println("error: " + FirebaseError.INVALID_EMAIL);
//                        System.out.println("error: " + firebaseError.getCode());
//
//                        //hack line to check if the user email is alread taken assume they are registered
//                        if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
//                            //already registered case
//                            passInput.setVisibility(View.VISIBLE);
//                            passInput.setHint(R.string.passwordHint);
//                            loginButton.setVisibility(View.VISIBLE);
//                            nextButton.setVisibility(View.GONE);
//                        }
//                        else {
//                            //have user register
//                            Toast.makeText(getApplicationContext(), R.string.registerToast, Toast.LENGTH_LONG).show();
//                            passInput.setVisibility(View.VISIBLE);
//                            passInput.setHint(R.string.setPasswordHint);
//                            registerButton.setVisibility(View.VISIBLE);
//                            nextButton.setVisibility(View.GONE);
//                        }
//                    }
//
//                    @Override
//                    public void onSuccess(Map<String, Object> stringObjectMap) {
//
//                    }
//                });
            }
        });

        //register new user
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //register user
                //users.put(usernameStr, passInput.getText().toString());

                //create a new firebase user
                myFirebaseRef.createUser(userInput.getText().toString(), passInput.getText().toString() , new Firebase.ValueResultHandler<Map<String,Object>>() {

                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        System.out.println("Successfully created user account with uid: " + result.get("uid"));
                        //authenticate user
                        myFirebaseRef.authWithPassword(userInput.getText().toString(), passInput.getText().toString(), new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                //authenticated successfully
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("provider", authData.getProvider());
                                //map.put("email", userInput.getText().toString());
                                myFirebaseRef.child("users").child(authData.getUid()).setValue(map);

                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                //something went wrong
                                Log.i("onAuthenticatedError", "Error authenticating");
                            }
                        });

                        Toast.makeText(getApplicationContext(), R.string.registerThanks, Toast.LENGTH_SHORT).show();

                        //Create Bundle to pass username to ControlActivity
                        loginBundle = new Bundle();
                        //assign the values (key, value pairs)
                        loginBundle.putString(context.getString(R.string.username), userInput.getText().toString());

                        //create intent for control activity
                        Intent intentControl = new Intent(LoginActivity.this, ControlActivity.class);

                        //assign the bundle to the intent
                        intentControl.putExtras(loginBundle);

                        //launch control activity
                        LoginActivity.this.startActivity(intentControl);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        // there was an error
                        Log.i("onError", "Error creating firebase user");
                        Toast.makeText(getApplicationContext(), R.string.registerError, Toast.LENGTH_LONG).show();
                        passInput.setText("");
                    }
                });
            }
        });

        //set login button callback for previously registered users
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //conditional to test if user and password are correct
                usernameStr = userInput.getText().toString();

                //authenticate user
                myFirebaseRef.authWithPassword(usernameStr, passInput.getText().toString(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        //authenticated successfully
                        //Map<String, String> map = new HashMap<String, String>();
                        //map.put("provider", authData.getProvider());
                        //map.put("more", "more");
                        //myFirebaseRef.child("users").child(authData.getUid()).setValue(map);

                        //Create Bundle to pass username to ControlActivity
                        loginBundle = new Bundle();
                        //assign the values (key, value pairs)
                        loginBundle.putString(context.getString(R.string.username), usernameStr);

                        //create intent for control activity
                        Intent intentControl = new Intent(LoginActivity.this, ControlActivity.class);

                        //assign the bundle to the intent
                        intentControl.putExtras(loginBundle);

                        //launch control activity
                        LoginActivity.this.startActivity(intentControl);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        System.out.println("error: " + firebaseError.INVALID_PASSWORD);
                        System.out.println("error: " + firebaseError.getCode());

                        //something went wrong
                        Log.i("onAuthenticatedError", "Error authenticating");
                        passInput.setText("");
                        //display toast for incorrect login attempt
                        Toast.makeText(getApplicationContext(), R.string.incorrectLogin, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}
