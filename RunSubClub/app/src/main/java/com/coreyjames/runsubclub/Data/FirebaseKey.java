package com.coreyjames.runsubclub.Data;

/**
 * Created by csteimel on 2/17/17.
 */

public enum FirebaseKey {

    DAY,
    DAYNUMBER,
    AUX,
    SESSIONTYPE,
    ISCOMPLETED;

    public String pathString() {

        switch (this) {
            case DAY: return "PATH_TO_DAY_NODE";
            case DAYNUMBER: return "PATH_TO_DAY_NUMBER_NODE";
            case AUX: return "PATH_TO_AUX";
            case SESSIONTYPE: return "PATH_TO_SESSION_TYPE";
            case ISCOMPLETED: return "plans/foundation/days/0/aux/isCompleted";
        }
        return "";
    }
}
