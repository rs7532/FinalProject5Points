package com.example.finalproject5points.Objects;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Train {
    public String trainName;
    public String trainingArea;
    public String trainingTime;
    public String coachName;

    public Train(){}
    public Train(String trainName, String trainingArea, String trainingTime, String coachName){
        this.trainName = trainName;
        this.trainingArea = trainingArea;
        this.trainingTime = trainingTime;
        this.coachName = coachName;
    }
    public Train(String trainName){
        this.trainName = trainName;
        this.trainingArea = "free";
        if(trainName.equals("Gym")){
            this.trainingTime = "-1";
        }
        else{
            this.trainingTime = "-2";
        }
        this.coachName = "None";
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTrainingArea() {
        return trainingArea;
    }

    public void setTrainingArea(String trainingArea) {
        this.trainingArea = trainingArea;
    }

    public String getTrainingTime() {
        return trainingTime;
    }

    public void setTrainingTime(String trainingTime) {
        this.trainingTime = trainingTime;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    @NonNull
    @Override
    public String toString() {
        if(this.trainName.equals("Gym")){
            return this.trainName + ", Entry is allowed at any time for any facility";
        }
        else if(this.trainName.equals("Pool")){
            return this.trainName + ", Entry is allowed at any time for any facility except gym";
        }
        else{
            String day = DayofWeek();
            String time = this.trainingTime.substring(1, 3) + ":" + this.trainingTime.substring(3);

            return this.trainName + " with coach " + this.coachName + " at the \n" + this.trainingArea + " on "
                    + day + " at " + time;
        }
    }

    /**
     * @return day of week by the trainingTime.
     */
    public String DayofWeek(){
        if (this.trainingTime.charAt(0) == '1') {
            return "Sunday";
        } else if (this.trainingTime.charAt(0) == '2') {
            return "Monday";
        } else if (this.trainingTime.charAt(0) == '3') {
            return "Tuesday";
        } else if (this.trainingTime.charAt(0) == '4') {
            return "Wednesday";
        } else if (this.trainingTime.charAt(0) == '5') {
            return "Thursday";
        } else if (this.trainingTime.charAt(0) == '6') {
            return "Friday";
        } else if (this.trainingTime.charAt(0) == '7'){
            return "Saturday";
        }
        return "";
    }
}
