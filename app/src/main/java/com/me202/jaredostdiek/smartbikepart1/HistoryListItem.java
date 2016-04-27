package com.me202.jaredostdiek.smartbikepart1;

/**
 * Created by jaredostdiek on 4/11/16.
 *File Description: Java class to package info in object for ride history listview
 */

public class HistoryListItem {
    //private int ID;
    private String date;
    private String location;
    private int iconID;
    private String fireID;

    public HistoryListItem(){
    }

    public HistoryListItem(String fireID, int iconID, String loc, String d){
        //this.ID = ID;
        this.iconID = iconID;
        this.location = loc;
        this.date = d;
        this.fireID = fireID;
    }

    public HistoryListItem(int iconID, String loc, String d){
        this.iconID = iconID;
        this.location = loc;
        this.date = d;
    }

//    public HistoryListItem(int iconID, String loc, String d, String fireID){
//        this.iconID = iconID;
//        this.location = loc;
//        this.date = d;
//        this.fireID = fireID;
//    }

    //get and set methods

    //public int getID() { return ID; }

    //public void setID(int ID) { this.ID = ID;}

    public int getIconID() {return iconID;}

    public void setIconID(int iconID) {this.iconID = iconID;}

    public String getLocation() {return location;}

    public void setLocation(String location) {this.location = location;}

    public String getDate() {return date; }

    public void setDate(String date) {this.date = date;}

    public void setFireID(String fireID) {this.fireID = fireID;}

    public String getFireID() {return fireID; }

}
