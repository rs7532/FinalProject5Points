package com.example.finalproject5points.Objects;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Membership {
    public String fullName;
    public String email;
    public String phone;
    public ArrayList<Train> trainsData;
    public boolean guard, admin;
    public Membership(){}
    public Membership(String fullName, String email, String phone, ArrayList<Train> trainsData, boolean guard, boolean admin){
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.trainsData = trainsData;
        this.guard = guard;
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public ArrayList<Train> getTrainsData() {
        return trainsData;
    }
    public void setTrainsData(ArrayList<Train> trainsData) {
        this.trainsData = trainsData;
    }
    public boolean isGuard() {
        return this.guard;
    }
    public void setGuard(boolean guard) {
        this.guard = guard;
    }
    public boolean isAdmin() {
        return this.admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @NonNull
    public String toString(){
        String tmp = this.fullName + "\n" + this.phone + "\n" + this.email + "\n";

        if (this.admin){
            tmp += "is an admin\n";
        }
        else{
            tmp += "is not an admin\n";
        }

        if (this.guard){
            tmp += "is a guard\n";
        }
        else{
            tmp += "is not a guard\n";
        }
        tmp += "\nTrains details:\n";

        for (int i = 0; i < this.trainsData.size(); i++){
            tmp += "- " + this.trainsData.get(i).toString() +"\n";
        }
        return tmp;
    }
}
