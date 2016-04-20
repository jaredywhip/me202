package com.me202.jaredostdiek.smartbikepart1;
//android imports
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//java imports
import java.util.ArrayList;
//library imports
import com.squareup.picasso.Picasso;

/**
 * Created by jaredostdiek on 4/11/16.
 *File Description: Custom array adapter class to display
 * ride history information in a ListView.
 */

//adaper compatible with sqlite database
public class HistoryArrayAdapter extends ArrayAdapter<HistoryListItem> {
    private Context context;
    private ArrayList<HistoryListItem> listOjects;

    // pass adapter the context and an array of objects containing info for the list
    public HistoryArrayAdapter(Context context, ArrayList<HistoryListItem> listOjects){
        super(context, R.layout.history_list_item, R.id.dateTextView, listOjects);
        this.context = context;
        this.listOjects = listOjects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        //inflate the layout for each row of the history list
        if(v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.history_list_item, null);
        }

        //get one object from the arraylist
        HistoryListItem singleItem = listOjects.get(position);

        //set views
        if (singleItem != null) {
            TextView locTextView = (TextView) v.findViewById(R.id.locTextView);
            TextView dateTextView = (TextView) v.findViewById(R.id.dateTextView);
            ImageView iconImageView = (ImageView) v.findViewById(R.id.iconImageView);

            locTextView.setText(singleItem.getLocation());
            dateTextView.setText(singleItem.getDate());
            Picasso.with(context).load(singleItem.getIconID()).fit().centerInside().into(iconImageView);
        }

        //return the view
        return v;
    }
}