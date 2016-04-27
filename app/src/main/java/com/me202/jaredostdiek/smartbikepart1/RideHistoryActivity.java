package com.me202.jaredostdiek.smartbikepart1;

//android imports
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
//java imports
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.text.SimpleDateFormat;
//library imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.AuthData;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.gc.materialdesign.views.ButtonFloat;

/**
 * Created by jaredostdiek on 4/4/16.
 *File Description: Java file to control ride history screen.
 */

public class RideHistoryActivity extends Activity {
    Context context = this;
    EditText locEditText;
    private HistoryArrayAdapter historyAdaptor;
    ButtonFloat addLocFloat;
    private HistoryListItem histItem;
    //create array of list item objects
    private ArrayList<HistoryListItem> histListItems = new ArrayList<HistoryListItem>();
    HistoryDatabaseHandler db;
    ArrayList<HistoryListItem> history;
    Firebase myFirebaseRef;
    Map<String, Object> historyMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        //create a reference to the Firebase database
        myFirebaseRef = new Firebase("https://vivid-heat-8090.firebaseio.com/");
        AuthData authData = myFirebaseRef.getAuth();
        final String userID = authData.getUid();
        System.out.println("userID: " + userID);
        db = new HistoryDatabaseHandler(context);
        db.deleteAllHistory();

        //instantiate our custom adapter
        historyAdaptor = new HistoryArrayAdapter(this, histListItems);
        final ListView listViewHistory = (ListView) findViewById(R.id.historyListView);
        listViewHistory.setAdapter(historyAdaptor);

        //create new tread to process image
        new Thread(new Runnable() {
            public void run() {
                //create bitmap from image with less resolution to reduce memory required
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap riderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.jaredheadshothdpi, options);

                //Add circle mask
                CircleCrop riderImageCircle = new CircleCrop();
                Bitmap riderBitmapCircle = riderImageCircle.transform(riderBitmap);

                //set imageview picture
                ImageView riderImage = (ImageView) findViewById(R.id.riderImageView);
                riderImage.setImageBitmap(riderBitmapCircle);
            }
            }).start();

        //set components needed to add new ride location to the list
        locEditText = (EditText) findViewById(R.id.locationEditText);
        addLocFloat = (ButtonFloat) findViewById(R.id.addLocButtonFloat);

        //reading data single query slide. add listener for single value event.
        //then add data to sql

//        myFirebaseRef.child("users").child(userID).child("rideHistory").addListenerForSingleValueEvent(new ValueEventListener() {
        myFirebaseRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //db.deleteAllHistory();
                System.out.println("dataSnapshot: " + dataSnapshot);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    HistoryListItem post = postSnapshot.getValue(HistoryListItem.class);
                    System.out.println("postSnapshot: " + post.getFireID());
                    db.addHistory(post);
                    histListItems.add(histItem);

                    //List<HistoryListItem> history = db.getAllItems();
                    history = db.getAllItems();

                    for (HistoryListItem hs : history) {
                        String log = "FireID: " + hs.getFireID() + " ,IconID: " + hs.getIconID() + " ,Location: " + hs.getLocation()
                                + " ,Date: " + hs.getDate();
                        //Writing Contacts to log
                        Log.d("Name: ", log);
                    }

                    historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
                    listViewHistory.setAdapter(historyAdaptor);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });






        //create listener for the button to add a ride location
        addLocFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String location = locEditText.getText().toString();

                if (!location.matches("")) {
                    //add items to HistoryListItem class
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    Random rand = new Random();
                    int iconRandom = rand.nextInt(2);

                    if (iconRandom == 0) {
                        iconRandom = R.drawable.fastclock;
                    } else {
                        iconRandom = R.drawable.beachcruiser;
                    }

                    //firebase id
                    String fireID = myFirebaseRef.child("users").child(userID).getKey();

                    //add item to the beginning of the list
                    histItem = new HistoryListItem(fireID, iconRandom, location, date);
                    histListItems.add(histItem);
                    db.addHistory(histItem);
                    //update history adapter
                    history = db.getAllItems();
                    historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
                    listViewHistory.setAdapter(historyAdaptor);

                    //add item to firebase

                    System.out.println("userID: " + userID);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("historyItem", histItem);
                    //historyMap = new ObjectMapper().convertValue(histItem, Map.class);
                    //myFirebaseRef.child("users").child(userID).child("rideHistory").push().setValue(histItem);
                    myFirebaseRef.child("users").child(userID).push().setValue(histItem);

//                    // callback
//                    myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            //add to sql arraylist
//                            // Inserting Contacts
//                            Log.d("Insert: ", "Inserting ..");
//                            db.addHistory(histItem);
//
//                            // Reading all contacts
//                            Log.d("Reading: ", "Reading all contacts..");
//                            history = db.getAllItems();
//
//                            for (HistoryListItem hs : history) {
//                                String log = "FireID: " + hs.getFireID() + " ,IconID: " + hs.getIconID() + " ,Location: " + hs.getLocation()
//                                        + " ,Date: " + hs.getDate();
//
//                                //Writing Contacts to log
//                                Log.d("Name: ", log);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(FirebaseError firebaseError) {
//
//                        }
//                    });





                    //clear textEdit field
                    locEditText.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.pleaseAddLoc, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //set TextWatcher for locEditText to show button only when text is input
        locEditText.addTextChangedListener(addLocWatcher);

        //delete items if long clicked
        listViewHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("long clicked", "pos: " + position);

                //delete item from list
                HistoryListItem deleteItem = history.get(position);
                db.deleteHistory(deleteItem);

                //update history adapter
                history = db.getAllItems();
                historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
                listViewHistory.setAdapter(historyAdaptor);

                //update the view
                historyAdaptor.notifyDataSetChanged();

                return false;
            }
        });

        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //delete sql database
        db.deleteAllHistory();
    }

    //watch to see if text is input into location textEdit
        private final TextWatcher addLocWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    addLocFloat.setVisibility(View.GONE);
            }
                else{
                    addLocFloat.setVisibility(View.VISIBLE);
                }
            }
        };
}
