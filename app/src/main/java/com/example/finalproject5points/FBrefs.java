package com.example.finalproject5points;

import static com.example.finalproject5points.Activities.LogIn.currentTrainee;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBrefs {
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static DatabaseReference refTrainees = FBDB.getReference("AllMemberships");
    public static DatabaseReference refEncryptionKey = FBDB.getReference("EncryptionKey");
    public static DatabaseReference refGotin = FBDB.getReference("GotIn");
    public static DatabaseReference NfcStr = FBDB.getReference("NfcStr");
    public static DatabaseReference refMembershipTrains;
    public static DatabaseReference refSports = FBDB.getReference("Sports");
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static String Uid = refAuth.getUid();
    public static FirebaseStorage stoarge = FirebaseStorage.getInstance();
    public static StorageReference storageReference = stoarge.getReference();

}
