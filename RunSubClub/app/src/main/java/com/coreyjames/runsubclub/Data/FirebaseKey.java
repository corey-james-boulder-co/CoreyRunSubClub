package com.coreyjames.runsubclub.Data;

/**
 * Created by csteimel on 2/17/17.
 */

public enum FirebaseKey {

    DAY;

    public String pathString() {
        switch (this) {
            case DAY: return "day";
        }
        return "";
    }
}
