package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.Activities.LogIn.currentTrainee;
import static com.example.finalproject5points.FBrefs.refAuth;
import static com.example.finalproject5points.FBrefs.refMembershipTrains;
import static com.example.finalproject5points.FBrefs.storageReference;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject5points.Objects.Train;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainMembershipActivity extends AppCompatActivity {

    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 14/12/2024
     * this activity is the main activity after signing in to the user, admin has option of
     add/update/remove users.
     */

    boolean isAdmin;
    ArrayList<Train> trainsDisplay;
    TextView details;


    /**
     * This function checks if the user that signed in is an admin,
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_membership);

        details = findViewById(R.id.TrainsDetails_Tv);
        details.setMovementMethod(new ScrollingMovementMethod());
        trainsDisplay = new ArrayList<>();

        checkAdmin();
        retrieveExistTrains();

    }

    /**
     * This function checks if the user is an admin.
     */
    private void checkAdmin(){
        currentTrainee.child("admin").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    isAdmin = (boolean) dataSnapshot.getValue();
                    invalidateOptionsMenu();
                }
            }
        });
    }

    /**
     * This function gets the user's Training data and presents it.
     */
    private void retrieveExistTrains(){
        refMembershipTrains.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DataSnapshot dS) {
                for (DataSnapshot dataSnapshot: dS.getChildren()){
                    Train tmp = dataSnapshot.getValue(Train.class);
                    Calendar calendar = Calendar.getInstance();
                    if (Integer.parseInt(String.valueOf(tmp.getTrainingTime().charAt(0))) >= calendar.get(Calendar.DAY_OF_WEEK) || tmp.getTrainName().equals("Gym")){
                        if (trainsDisplay.size() < 3){
                            trainsDisplay.add(tmp);
                            details.setText(details.getText() + "\n" +
                                    "\n • " + tmp.toString());
                        }
                    }
                }
            }
        });
    }

    /**
     * This function creates the menu.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if(isAdmin){
            menu.add("Add new membership");
            menu.add("See all subscriptions");
            menu.add("Update a membership");
        }
        menu.add("Sign out");
        return true;
    }

    /**
     * This function routes between the activities by the user choice.
     *
     * @param item The menu item that was selected.
     *
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String st = item.getTitle().toString();
        Intent intent = new Intent();
        if(st.equals("Add new membership")){
            intent = new Intent(this, AddMembership.class);
        }
        else if (st.equals("Sign out")){
            refAuth.signOut();
            intent = new Intent(this, LogIn.class);
            SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Email", "");
            editor.putString("Password", "");
            editor.commit();
        }
        else if(st.equals("See all subscriptions")){
            intent = new Intent(this, seeMemberships.class);
        }
        else if(st.equals("Update a membership")){
            intent = new Intent(this, seeMemberships.class);
            intent.putExtra("update", true);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    public void qrCode_pressed(View view) {
        Intent intent = new Intent(MainMembershipActivity.this, getIn_SportsCenter.class);
        startActivity(intent);
    }
}