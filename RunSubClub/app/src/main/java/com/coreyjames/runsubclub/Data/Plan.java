package com.coreyjames.runsubclub.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/16/17.
 */

public class Plan {

    PlanType planType;

    public Plan(PlanType planType) {
        this.planType = planType;
    }

    public Object encode(){
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("planType", planType.encode());
        return encoding;
    }

    public static Plan decode(Object json){
        Map<String, Object> encoding = (Map<String, Object>) json;
        String planTypeEncoding = (String) encoding.get("planType");
        PlanType planType = PlanType.decode(planTypeEncoding);
        if (planType != null) {
            return new Plan(planType);
        }
        return null;
    }

}
