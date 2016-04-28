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
import java.util.Random;
import java.text.SimpleDateFormat;
//library imports
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
    HistorySQLHandler db;
    ArrayList<HistoryListItem> history;
    Firebase myFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        //create a reference to the Firebase database
        myFirebaseRef = new Firebase("https://vivid-heat-8090.firebaseio.com/");

        //get current firebase user
        AuthData authData = myFirebaseRef.getAuth();
        final String userID = authData.getUid();
        System.out.println("userID: " + userID);

        //create sql database and delete any previously stored values
        db = new HistorySQLHandler(context);
        db.deleteAllHistory();

        //instantiate our custom adapter for the listview
        historyAdaptor = new HistoryArrayAdapter(this, histListItems);
        final ListView listViewHistory = (ListView) findViewById(R.id.historyListView);
        listViewHistory.setAdapter(historyAdaptor);

        //create new tread to process user image
        new Thread(new Runnable() {
            public void run() {
                //create bitmap from image with less resolution to reduce memory required
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap riderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.jaredheadshothdpi, options);

                //Add circle mask
                CircleCrop riderImageCircle = new CircleCrop();
                final Bitmap riderBitmapCircle = riderImageCircle.transform(riderBitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //set imageview picture
                        ImageView riderImage = (ImageView) findViewById(R.id.riderImageView);
                        riderImage.setImageBitmap(riderBitmapCircle);
                    }
                });
            }
            }).start();

        //set components needed to add new ride location to the list
        locEditText = (EditText) findViewById(R.id.locationEditText);
        addLocFloat = (ButtonFloat) findViewById(R.id.addLocButtonFloat);

        //add firebase data to local sql data base. run only once.
        myFirebaseRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("dataSnapshot: " + dataSnapshot);

                //get users rides and store in sql table
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    HistoryListItem post = postSnapshot.getValue(HistoryListItem.class);
                    db.addHistory(post);
                    histListItems.add(post);
                }

                //add history to local sql and load listview
                history = db.getAllItems();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
                        listViewHistory.setAdapter(historyAdaptor);
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("onCancelled: ", firebaseError.toString());
            }
        });

        //create listener for the button to add a ride location
        addLocFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get user input location
                String location = locEditText.getText().toString();

                if (!location.matches("")) {

                    //add items to HistoryListItem class
                    final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    Random rand = new Random();
                    int iconRandom = rand.nextInt(2);
                    if (iconRandom == 0) {
                        iconRandom = R.drawable.fastclock;
                    } else {
                        iconRandom = R.drawable.beachcruiser;
                    }

                    //create new ride history item
                    histItem = new HistoryListItem(iconRandom, location, date);

                    //create map of ride object
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("historyItem", histItem);

                    //get unique storage id from firebase for history item
                    Firebase ridePostRef = myFirebaseRef.child("users").child(userID);
                    Firebase newRidePostRef = ridePostRef.push();
                    newRidePostRef.setValue(histItem);

                    // Get the unique ID generated by push()
                    String postId = newRidePostRef.getKey();

                    //store the unique firebase id in the item
                    histItem.setFireID(postId);
                    newRidePostRef.setValue(histItem);

                    //add to ride history array
                    histListItems.add(histItem);
                    db.addHistory(histItem);

                    //update history adapter for the listview
                    history = db.getAllItems();
                    historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
                    listViewHistory.setAdapter(historyAdaptor);

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

                //delete item from list
                HistoryListItem deleteItem = historyAdaptor.getItem(position); //history.get(position);

                //remove item from firebase database
                myFirebaseRef.child("users").child(userID).child(deleteItem.getFireID()).removeValue();

                //remove item for sql
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
    protected void onResume() {
        super.onResume();

        //load sql data base on resume
        history = db.getAllItems();
        historyAdaptor = new HistoryArrayAdapter(RideHistoryActivity.this, history);
        final ListView listViewHistory = (ListView) findViewById(R.id.historyListView);
        listViewHistory.setAdapter(historyAdaptor);
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
