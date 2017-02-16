package com.coreyjames.runsubclub;

/**
 * Created by csteimel on 2/6/17.
 */

public class UserFirebaseDataHelper {

    public int completionStatus;
    public String completionStatusText;


    public UserFirebaseDataHelper() {
    }

    public UserFirebaseDataHelper(String completionStatusText, int completionStatus) {
        this.completionStatus = completionStatus;
        this.completionStatusText = completionStatusText;
    }


    public int getCompletionStatus() {return completionStatus;}
    public String getCompletionStatusText() {return completionStatusText;}

}
