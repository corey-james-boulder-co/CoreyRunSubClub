package com.coreyjames.runsubclub.Data;

/**
 * Created by csteimel on 2/16/17.
 */

public enum PlanType {

    BASIC,
    FOUNDATION,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    public String encode(){
        switch (this) {
            case BASIC: return "basic";
            case FOUNDATION: return "foundation";
            case BEGINNER: return "beginner";
            case INTERMEDIATE: return "intermediate";
            case ADVANCED: return "advanced";
        }
        return "";
    }

    public static PlanType decode(String encoding){
        switch (encoding) {
            case "basic": return BASIC;
            case "foundation": return FOUNDATION;
            case "beginner": return BEGINNER;
            case "intermediate": return INTERMEDIATE;
            case "advanced": return ADVANCED;
        }
        return BASIC;
    }

}
