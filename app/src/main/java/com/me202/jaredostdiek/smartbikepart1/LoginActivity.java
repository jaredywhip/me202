package com.me202.jaredostdiek.smartbikepart1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control the Login Screen.
 */

public class LoginActivity extends AppCompatActivity {

    Button loginButton, nextButton, registerButton;
    EditText userInput, passInput;
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

        //create objects for button and user inputs
        loginButton = (Button)findViewById(R.id.loginButton);
        userInput = (EditText)findViewById(R.id.username);
        passInput = (EditText)findViewById(R.id.password);
        nextButton = (Button) findViewById(R.id.nextButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        //when next button is pressed determine if already a user or need to register
        nextButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  usernameStr = userInput.getText().toString();

                  //hack line to test if email is already in database
                  myFirebaseRef.authWithPassword(usernameStr, "aakdafnngnkdjf129nf8vJaMmmadksn", new Firebase.AuthResultHandler() {
                      @Override
                      public void onAuthenticated(AuthData authData) {
                      }

                      //examine error code to determine if registered
                      @Override
                      public void onAuthenticationError(FirebaseError firebaseError) {

                          System.out.println("UserDoesNotExist: " + FirebaseError.USER_DOES_NOT_EXIST);
                          System.out.println("InvalidPassword: " + FirebaseError.INVALID_PASSWORD);
                          System.out.println("error: " + firebaseError.getCode());

                          switch (firebaseError.getCode()) {

                              //show user to register buttons
                              case FirebaseError.USER_DOES_NOT_EXIST:
                                  // have user register
                                  Toast.makeText(getApplicationContext(), R.string.registerToast, Toast.LENGTH_LONG).show();
                                  passInput.setVisibility(View.VISIBLE);
                                  passInput.setHint(R.string.setPasswordHint);
                                  registerButton.setVisibility(View.VISIBLE);
                                  nextButton.setVisibility(View.GONE);
                                  break;

                              //already registered. show login buttons
                              case FirebaseError.INVALID_PASSWORD:
                                  //already registered case
                                  passInput.setVisibility(View.VISIBLE);
                                  passInput.setHint(R.string.passwordHint);
                                  loginButton.setVisibility(View.VISIBLE);
                                  nextButton.setVisibility(View.GONE);
                                  break;

                              default:
                                  // handle other errors
                                  break;
                          }
                      }
                  });
              }
          });

        //register new user
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create a new firebase user
                myFirebaseRef.createUser(userInput.getText().toString(), passInput.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {

                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        System.out.println("Successfully created user account with uid: " + result.get("uid"));

                        //authenticate user
                        myFirebaseRef.authWithPassword(userInput.getText().toString(), passInput.getText().toString(), new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                //authenticated successfully
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

                        if (firebaseError.getCode() == firebaseError.INVALID_PASSWORD) {
                            passInput.setText("");
                            //display toast for incorrect login attempt
                            Toast.makeText(getApplicationContext(), R.string.incorrectLogin, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.somethingWrong, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}