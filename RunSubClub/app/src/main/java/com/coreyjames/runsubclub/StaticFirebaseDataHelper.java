package com.coreyjames.runsubclub;

/**
 * Created by csteimel on 1/26/17.
 */

public class StaticFirebaseDataHelper {

    public String sessionType;
    public String description;
    public String duration;
    public String workoutInstructionText;
    public String workoutInstructionVideo;



    public StaticFirebaseDataHelper() {
    }

    public StaticFirebaseDataHelper(String sessionType, String description, String duration,
                                    String workoutInstructionText, String workoutInstructionVideo) {
        this.sessionType = sessionType;
        this.description = description;
        this.duration = duration;
        this.workoutInstructionText = workoutInstructionText;
        this.workoutInstructionVideo = workoutInstructionVideo;
    }

    public String getSessionType() {return sessionType;}
    public String getDescription() {return description;}
    public String getDuration() {return duration;}
    public String getWorkoutInstructionText() {return workoutInstructionText;}
    public String getWorkoutInstructionVideo() {return workoutInstructionVideo;}

}

