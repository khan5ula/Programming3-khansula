package com.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class WarningMessage {
    private String nickname;
    private double latitude;
    private double longitude;
    private String dangertype;
    private LocalDateTime sent;
    private String areacode;
    private String phonenumber;
    private boolean areaAndphone = false;

    /**
     * Constructs a new WarningMessage object with the given parameters.
     * This constructor has the minimum parameters required for a WarningMessage
     * 
     * @param nickname: The nickname of the user who sent the warning message. It doesn't have to be same than the username. User can have multiple nicknames.
     * @param latitude: Latitude of the location associated with the warning message
     * @param longitude: Longitude of the location associated with the warning message
     * @param dangertype: Type of danger associated with the warning message
     * @param sent: Date and time at which the warning message was sent
     */    
    public WarningMessage(String nickname, double latitude, double longitude, String dangertype, LocalDateTime sent) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
    }

    /**
     * Constructs a new WarningMessage object with the given parameters.
     * Takes two additional parameters: areacode and phonenumber
     *
     * @param nickname: The nickname of the user who sent the warning message. It doesn't have to be same than the username. User can have multiple nicknames.
     * @param latitude: Latitude of the location associated with the warning message
     * @param longitude: Longitude of the location associated with the warning message
     * @param dangertype: Type of danger associated with the warning message
     * @param sent: Date and time at which the warning message was sent
     * @param areacode: The area code from where the WarningMessage was sent (for example: "358")
     * @param phonenumber: The phone number associated with the warning message. Portrayed as String. The format is not checked.
     */
    public WarningMessage(String nickname, double latitude, double longitude, String dangertype, LocalDateTime sent, String areacode, String phonenumber) {
        this(nickname, latitude, longitude, dangertype, sent); // call to the default constructor with five parameters
        this.areacode = areacode;
        this.phonenumber = phonenumber;
        this.areaAndphone = true;
    }
    public void setNick(String nickname) {
        this.nickname = nickname;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDangertype(String dangerString) {
        this.dangertype = dangerString;
    }

    public String getNickname() {
        return this.nickname;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getDangertype() {
        return this.dangertype;
    }

    public long dateAsInt() {
        return this.sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public ZonedDateTime getSent(ZoneOffset zone) {
        return this.sent.atZone(zone);
    }

    public static LocalDateTime setSent(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAreacode() {
        return areacode;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public boolean hasItAreaAndPhone() {
        return areaAndphone;
    }
}
