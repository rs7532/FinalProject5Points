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
        this.trainingTime = "0";
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
        else{
            String day = "";
            String time = this.trainingTime.substring(1, 3) + ":" + this.trainingTime.substring(3);
            if (this.trainingTime.charAt(0) == '1') {
                day = "Sunday";
            } else if (this.trainingTime.charAt(0) == '2') {
                day = "Monday";
            } else if (this.trainingTime.charAt(0) == '3') {
                day = "Tuesday";
            } else if (this.trainingTime.charAt(0) == '4') {
                day = "Wednesday";
            } else if (this.trainingTime.charAt(0) == '5') {
                day = "Thursday";
            } else if (this.trainingTime.charAt(0) == '6') {
                day = "Friday";
            } else if (this.trainingTime.charAt(0) == '7'){
                day = "Saturday";
            }
            return this.trainName + ", " + this.coachName + ", " + this.trainingArea + ", " + day + ", " + time;
        }
    }
}
