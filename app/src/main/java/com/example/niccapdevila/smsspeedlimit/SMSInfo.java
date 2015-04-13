package com.example.niccapdevila.smsspeedlimit;


/**
 * Created by niccapdevila on 4/6/15.
 */
public class SMSInfo {

    private String ID;
    private String date;
    private String address;
    private String speed;

    public SMSInfo() {
    }

    public SMSInfo(String ID, String date, String address) {
        this.ID = ID;
        this.date = date;
        this.address = address;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
