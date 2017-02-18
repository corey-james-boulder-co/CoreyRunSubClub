package com.coreyjames.runsubclub.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/17/17.
 */

public class Day extends FirebaseSerializable {

    FirebaseKey firebaseKey = FirebaseKey.DAY;
    Long dayId;
    Date date;
    Session aux;
    Session run;

    public Day (Long dayId, Date date, Session aux, Session run) {
        this.dayId = dayId;
        this.date = date;
        this.aux = aux;
        this.run = run;
    }

    public Object encode() {
        Map<String,Object> encoding = new HashMap<>();
        encoding.put("dayId", dayId);
        encoding.put("date", FormatHelper.dateToString(date));
        encoding.put("aux", aux.encode());
        encoding.put("run", run.encode());
        return encoding;
    }

    public static Day decode(Object json) {
        Map<String, Object> encoding = (Map<String, Object>) json;
        if (encoding != null) {
            Long dayId = (Long) encoding.get("dayId");
            Date date = FormatHelper.stringToDate(encoding.get("date"));
            Session aux = Session.decode(encoding.get("aux"));
            Session run = Session.decode(encoding.get("run"));
            if (dayId != null && date != null && aux != null && run != null){
                return new Day(dayId, date, aux, run);
            }
        }
        return null;
    }
}
