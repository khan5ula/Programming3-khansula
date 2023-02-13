package com.server;

import java.util.ArrayList;

public class WarningMessage {
    private String nick;
    private String latitude;
    private String longitude;
    private String dangertype;
    ArrayList<WarningMessage> warningList;

    public WarningMessage(String nick, String latitude, String longitude, String dangertype) {
        this.nick = nick;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        warningList = new ArrayList<WarningMessage>();
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDangertype(String dangerString) {
        this.dangertype = dangerString;
    }

    public String getNick() {
        return this.nick;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public String getDangertype() {
        return this.dangertype;
    }
    
}
