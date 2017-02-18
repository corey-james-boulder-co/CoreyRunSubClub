package com.coreyjames.runsubclub.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/16/17.
 */

public class User {

    String uid;
    Map<PlanType, Plan> plans;
    PlanType activePlanType;
    Profile profile;

    public User(String uid, Map<PlanType, Plan> plans, PlanType activePlanType, Profile profile){
        this.uid = uid;
        this.plans = plans;
        this.activePlanType = activePlanType;
        this.profile = profile;
    }

    public Object encode(){
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("uid", uid);
        encoding.put("plans", encodePlans());
        encoding.put("activePlanType", activePlanType.encode());
        encoding.put("profile", profile.encode());
        return encoding;
    }

    public static User decode(Object json){
        Map<String, Object> encoding = (Map<String, Object>) json;
        if (encoding != null) {
            String uid = (String) encoding.get("uid");
            Profile profile = Profile.decode(encoding.get("profile"));
            PlanType activePlanType = PlanType.decode(encoding.get("activePlanType"));
            Map<PlanType, Plan> plans = decodePlans(encoding.get("plans"));
            if (uid != null && profile != null && activePlanType != null && plans != null) {
                return new User(uid, plans, activePlanType, profile);
            }
        }
        return null;
    }

    private static Map<PlanType, Plan> decodePlans(Object json){
        Map<String, Object> encoding = (Map<String, Object>) json;
        if (encoding != null) {
            Map<PlanType, Plan> plans = new HashMap<>();
            for (Map.Entry<String,Object> entry : encoding.entrySet()) {
                plans.put(PlanType.decode(entry.getKey()), Plan.decode(entry.getValue()));
            }
            return plans;
        }
        return null;
    }

    private Map<String, Object> encodePlans(){
        Map<String, Object> encoding = new HashMap<>();
        for (Map.Entry<PlanType, Plan> entry : plans.entrySet()) {
            encoding.put(entry.getKey().encode(), entry.getValue().encode());
        }
        return encoding;
    }

}