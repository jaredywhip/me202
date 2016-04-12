package com.me202.jaredostdiek.smartbikepart1;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Random;
import com.squareup.picasso.Picasso;
/**
 * Created by jaredostdiek on 4/11/16.
 */
public class HistoryArrayAdapter extends ArrayAdapter{
    private Context context;
    private ArrayList<HistoryListItem> listOjects;


    public HistoryArrayAdapter(Context context, ArrayList<HistoryListItem> listOjects){
        super(context, R.layout.history_list_item, R.id.dateTextView, listOjects);
        this.context = context;
        this.listOjects = listOjects;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if(v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.history_list_item, null);
        }

        HistoryListItem singleItem = listOjects.get(position);

        if (singleItem != null) {
            TextView locTextView = (TextView) v.findViewById(R.id.locTextView);
            TextView dateTextView = (TextView) v.findViewById(R.id.dateTextView);
            ImageView iconImageView = (ImageView) v.findViewById(R.id.iconImageView);

            locTextView.setText(singleItem.getLocation());
            dateTextView.setText(singleItem.getDate());
            Picasso.with(context).load(singleItem.getIconID()).fit().centerInside().into(iconImageView);
        }


        return v;
    }
}
