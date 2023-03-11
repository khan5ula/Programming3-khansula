package com.server;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Class that wraps the information required by a Time Query object.
 */
public class TimeQuery {
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    /**
     * Constructor that initialized the class variables.
     * @param timeStart LocalDateTime, the start time for the query
     * @param timeEnd LocalDateTime, the end time for the query
     */
    public TimeQuery(LocalDateTime timeStart, LocalDateTime timeEnd) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    /**
     * Gets the class variable timeStart as ZonedDateTime.
     * <p>The format is: 'timeStart + zone'
     * @param zone ZoneOffset, The time zone offset
     * @return ZonedDateTime timeStart + zone
     */
    public ZonedDateTime getTimeStart(ZoneOffset zone) {
        return this.timeStart.atZone(zone);
    }

    /**
     * Gets the class variable timeEnd as ZonedDateTime.
     * <p>The format is: 'timeEnd + zone'
     * @param zone ZoneOffset, The time zone offset
     * @return ZonedDateTime timeEnd + zone
     */
    public ZonedDateTime getTimeEnd(ZoneOffset zone) {
        return this.timeEnd.atZone(zone);
    }

    /**
     * Gets the class variable timeStart in Unix time format.
     * @return long, the time in Unix time
     */
    public long timeStartAsInt() {
        return this.timeStart.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * Gets the class variable timeEnd in Unix time format.
     * @return long, the time in Unix time
     */
    public long timeEndAsInt() {
        return this.timeEnd.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}