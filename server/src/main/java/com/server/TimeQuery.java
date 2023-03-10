package com.server;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeQuery {
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    public TimeQuery(LocalDateTime timeStart, LocalDateTime timeEnd) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public ZonedDateTime getTimeStart(ZoneOffset zone) {
        return this.timeStart.atZone(zone);
    }

    public ZonedDateTime getTimeEnd(ZoneOffset zone) {
        return this.timeEnd.atZone(zone);
    }

    public long timeStartAsInt() {
        return this.timeStart.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public long timeEndAsInt() {
        return this.timeEnd.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}