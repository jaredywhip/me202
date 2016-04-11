package com.me202.jaredostdiek.smartbikepart1;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import com.squareup.picasso.Picasso;
/**
 * Created by jaredostdiek on 4/11/16.
 */
public class HistoryArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ArrayList<String> historyInfo;


    public HistoryArrayAdapter(Context context, ArrayList<String> historyInfo){
        super(context,R.layout.history_list_item, historyInfo);
        this.context = context;
        this.historyInfo = historyInfo;

    }

    //@Override
    public View getHistoryView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if(v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.history_list_item, parent, false);
        }

        TextView locTextView = (TextView) v.findViewById(R.id.locTextView);
        TextView dateTextView = (TextView) v.findViewById(R.id.dateTextView);
        ImageView iconImageView = (ImageView) v.findViewById(R.id.iconImageView);

        locTextView.setText(historyInfo.get(position));
        dateTextView.setText(historyInfo.get(position));

        Random rand = new Random();
        int iconRandom = rand.nextInt(2);

        if (iconRandom == 0){
            Picasso.with(context).load(R.drawable.fastclock).resize(80,80).centerCrop().into(iconImageView);
        }
        else {
            Picasso.with(context).load(R.drawable.beachcruiser).resize(80,80).centerCrop().into(iconImageView);

        }

        return v;
    }
}
