package com.coreyjames.runsubclub.Data;

/**
 * Created by csteimel on 2/17/17.
 */

public enum SessionType {

    AEROBIC,
    DRILLS,
    FAST,
    RACEPACE,
    TRACK,
    TIMETRIAL,
    LONG,
    STRENGTH,
    MAINTENANCE;

    public String encode() {
        switch (this) {
            case AEROBIC:
                return "aerobic";
            case DRILLS:
                return "drills";
            case FAST:
                return "fast";
            case RACEPACE:
                return "racePace";
            case TRACK:
                return "track";
            case TIMETRIAL:
                return "timeTrial";
            case LONG:
                return "long";
            case STRENGTH:
                return "strength";
            case MAINTENANCE:
                return "maintenance";
        }
        return "";
    }

    public static SessionType decode(Object json) {
        String sessionType = (String) json;
        if (sessionType != null) {
            switch (sessionType) {
                case "aerobic":
                    return AEROBIC;
                case "drills":
                    return DRILLS;
                case "fast":
                    return FAST;
                case "racePace":
                    return RACEPACE;
                case "track":
                    return TRACK;
                case "timeTrial":
                    return TIMETRIAL;
                case "long":
                    return LONG;
                case "strength":
                    return STRENGTH;
                case "maintenance":
                    return MAINTENANCE;
            }
        }
        return null;
    }
}
