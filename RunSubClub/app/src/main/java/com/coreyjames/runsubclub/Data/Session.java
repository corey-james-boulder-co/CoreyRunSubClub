package com.coreyjames.runsubclub.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/17/17.
 */

public class Session {

    String comment;
    Long dayId;
    Double duration;
    Boolean isCompleted;
    String prescription;
    SessionType sessionType;
    String title;

    public Session (String comment, Long dayId, Double duration, Boolean isCompleted,
                    String prescription, SessionType sessionType, String title) {
        this.comment = comment;
        this.dayId = dayId;
        this.duration = duration;
        this.isCompleted = isCompleted;
        this.prescription = prescription;
        this.sessionType = sessionType;
        this.title = title;
    }

    public Object encode() {
        Map<String,Object> encoding = new HashMap<>();
        encoding.put("comment", comment);
        encoding.put("dayId", dayId);
        encoding.put("duration", duration);
        encoding.put("isCompleted", isCompleted);
        encoding.put("prescription", prescription);
        encoding.put("sessionType", sessionType.encode());
        encoding.put("title", title);
        return encoding;
    }

    public static Session decode(Object json) {
        Map<String,Object> encoding = (Map<String,Object>) json;
        if (encoding != null) {
            String comment = (String) encoding.get("comment");
            Long dayId = (Long) encoding.get("dayId");
            Double duration = (Double) encoding.get("duration");
            Boolean isCompleted = (Boolean) encoding.get("isCompleted");
            String prescription = (String) encoding.get("prescription");
            SessionType sessionType = SessionType.decode(encoding.get("sessionType"));
            String title = (String) encoding.get("title");
            if (comment != null && dayId != null && duration != null && isCompleted != null &&
                    prescription != null && sessionType != null && title != null) {
                return new Session(comment, dayId, duration, isCompleted, prescription, sessionType, title);
            }

        }
        return null;
    }

}
