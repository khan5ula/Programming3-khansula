package com.server;

import java.util.Arrays;
import java.util.Collections;
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

    /*
    * The default constructor requires parameters for all class variables
    */
    public WarningMessage(String nickname, double latitude, double longitude, String dangertype, LocalDateTime sent) {
        /* Check all required parameters were received */
        Object [] args = {nickname, latitude, longitude, dangertype, sent};
        if (Collections.frequency(Arrays.asList(args), null) >= 1) {
            throw new IllegalArgumentException("Error: Tried to give null parameter to new warning");
        }

        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
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
}
