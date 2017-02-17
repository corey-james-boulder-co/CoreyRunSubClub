package com.coreyjames.runsubclub.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by csteimel on 2/16/17.
 */

public class Profile {

    String name;
    Boolean hasOnboarded;

    public Profile(String name, Boolean hasOnboarded){
        this.name = name;
        this.hasOnboarded = hasOnboarded;
    }

    public Object encode(){
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("name", name);
        encoding.put("hasOnboarded", hasOnboarded);
        return encoding;
    }

    public static Profile decode(Object json){
        Map<String, Object> encoding = (Map<String, Object>) json;
        String name = (String) encoding.get("name");
        Boolean hasOnboarded = (Boolean) encoding.get("hasOnboarded");
        if (name != null && hasOnboarded != null) {
            return new Profile(name, hasOnboarded);
        }
        return null;
    }

}
