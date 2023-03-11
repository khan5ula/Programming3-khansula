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
    private int weather;

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
     * Setter for the class variable nickname.
     * @param nickname String, nickname that will be set to the class variable nickname.
     */
    public void setNick(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Setter for the class variable latitude.
     * @param latitude double, value that will be set to the class variable latitude.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Setter for the class variable longitude.
     * @param longitude double, value that will be set to the class variable longitude.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Setter for the class variable dangerString.
     * @param dangerString String, value that will be set to the class variable dangerString.
     */
    public void setDangertype(String dangerString) {
        this.dangertype = dangerString;
    }

    /**
     * Getter for the class variable nickname.
     * @return String, the class variable nickname.
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * Getter for the class variable latitude.
     * @return double, the class variable latitude.
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Getter for the class variable longitude.
     * @return double, the class variable longitude.
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Getter for the class variable dangertype.
     * @return String, the class variable dangertype.
     */
    public String getDangertype() {
        return this.dangertype;
    }

    /**
     * Setter for the class variable weather.
     * @param weather int, the class variable weather.
     */
    public void setWeather(int weather) {
        this.weather = weather;
    }

    /**
     * Getter for the class variable weather.
     * @return int, the class variable weather.
     */
    public int getWeather() {
        return this.weather;
    }

    /**
     * Transforms the class member variable LocalDateTime sent 
     * into Unix epoch format
     * @return this.sent as Long
     */
    public long dateAsInt() {
        return this.sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * Takes a time-zone offset and returns a sent class member value 
     * combined with the given timezone (ZonedDateTime)
     * @param zone The time-zone offset
     * @return ZonedDateTime object (this.sent + zone)
     */
    public ZonedDateTime getSent(ZoneOffset zone) {
        return this.sent.atZone(zone);
    }

    /**
     * Takes Unix epoch time value in Long format, and transforms it into LocalDateTime
     * @param epoch The unix time value as Long
     * @return LocalDateTime object
     */
    public static LocalDateTime setSent(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    /**
     * Setter for the class variable areacode.
     * @param areacode String, value that will be set to the class variable areacode.
     */
    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    /**
     * Setter for the class variable phonenumber.
     * @param phonenumber String, value that will be set to the class variable phonenumber.
     */
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    /**
     * Getter for the class variable areacode.
     * @return String, the class variable areacode.
     */
    public String getAreacode() {
        return areacode;
    }

    /**
     * Getter for the class variable phonenumber.
     * @return the class variable phonenumber.
     */
    public String getPhonenumber() {
        return phonenumber;
    }

    /**
     * Returns the value of class member variable areaAndPhone.
     * <p>Used for a quick check if the message has areacode and phonenumber.
     * @return boolean, value from member variable areaAndPhone
     */
    public boolean hasItAreaAndPhone() {
        return areaAndphone;
    }
}
