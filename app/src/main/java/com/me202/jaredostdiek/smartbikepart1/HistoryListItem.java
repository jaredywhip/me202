package com.me202.jaredostdiek.smartbikepart1;

/**
 * Created by jaredostdiek on 4/11/16.
 */

//this class is used to create an object to add to the listview for ride history
public class HistoryListItem {
    private String date;
    private String location;
    private int iconID;

    public HistoryListItem(){

    }

    public HistoryListItem(int ID, String loc, String d){
        this.iconID = ID;
        this.location = loc;
        this.date = d;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
