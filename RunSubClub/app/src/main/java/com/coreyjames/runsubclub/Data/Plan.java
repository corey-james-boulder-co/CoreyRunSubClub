package com.coreyjames.runsubclub.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/16/17.
 */

public class Plan {

    PlanType planType;
    Day[] days;

    public Plan(PlanType planType, Day[] days) {
        this.planType = planType;
        this.days = days;
    }

    public Object encode(){
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("planType", planType.encode());
        encoding.put("days", encodeDays());
        return encoding;
    }

    public static Plan decode(Object json){
        Map<String, Object> encoding = (Map<String, Object>) json;
        if (encoding != null) {
            PlanType planType = PlanType.decode(encoding.get("planType"));
            Day[] days = decodeDays(encoding.get("days"));
            if (planType != null && days != null) {
                return new Plan(planType, days);
            }
        }
        return null;
    }

    private Object encodeDays() {
        Object[] objects = {};
        for (Day day: days) {
            int index = day.dayId.intValue();
            objects[index] = day.encode();
        }
        return objects;
    }

    private static Day[] decodeDays(Object json) {
        Object[] objects = (Object[]) json;
        if (objects != null) {
            Day[] days = {};
            for (Object object: objects) {
                Day day = Day.decode(object);
                if (day != null) {
                    days[day.dayId.intValue()] = day;
                }
            }
            return days;
        }
       return null;
    }

}
