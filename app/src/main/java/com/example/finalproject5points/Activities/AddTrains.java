package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.FBrefs.refTrainees;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject5points.CustomAdapterMembership;
import com.example.finalproject5points.CustomAdapterTrains;
import com.example.finalproject5points.Objects.Train;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;


import java.util.ArrayList;

public class AddTrains extends AppCompatActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener{
    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 2/1/2025
     * This activity is for adding new user's training data.
     */
    Button saveBtn, addTimeBtn;
    ListView trainingDataLv;
    EditText coachNameEt;
    ArrayList<String> trainsTimes;
    ArrayList<Train> trainsObjArray;
    ArrayList<String> trainsData;
    String[] daysLst, sports;
    int hour, minutes, pos;
    Spinner sportsSpin, daysSpin;
    String trainName, trainArea;
    CustomAdapterTrains trainingDataSpinadp;
    Intent gi;
    String uidUpdate;

    /**
     * This function sets adapters for the spinners and listview, and gets uid user if it for updating
     exist user.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trains);

        gi = getIntent();
        uidUpdate = gi.getStringExtra("gotUid");

        saveBtn = findViewById(R.id.saveTrainsBtn);
        addTimeBtn = findViewById(R.id.addtimeBtn);
        trainingDataLv = findViewById(R.id.timeDayLv);
        coachNameEt = findViewById(R.id.coachnameET);
        trainsData = new ArrayList<>();
        trainsTimes = new ArrayList<>();
        trainsObjArray = new ArrayList<>();
        daysLst = new String[]{"choose day", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        sports = new String[]{"choose sport", "Tennis class", "Volleyball class", "Basketball class","Pool class", "Gym", "Pool"};
        sportsSpin = findViewById(R.id.SportsSpin);
        daysSpin = findViewById(R.id.daysSpin);

        hour = 0;
        minutes = 0;

        CustomAdapterTrains sportsSpinadp = new CustomAdapterTrains(this, sports);
        sportsSpin.setAdapter(sportsSpinadp);
        sportsSpin.setOnItemSelectedListener(this);

        CustomAdapterTrains daysSpinadp = new CustomAdapterTrains(this, daysLst);
        daysSpin.setAdapter(daysSpinadp);
        daysSpin.setOnItemSelectedListener(this);

        trainingDataSpinadp = new CustomAdapterTrains(this, trainsData);
        trainingDataLv.setAdapter(trainingDataSpinadp);
        trainingDataLv.setOnItemClickListener(this);
        trainingDataLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    /**
     * This function is for get the trains data of the exist user if it updates exist user, or gets
     exist data if the admin added to new user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        trainsData.clear();
        if (uidUpdate != null) {
            refTrainees.child(uidUpdate).child("trainsData").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        addTrainDetails(data);
                    }
                }
            });
        }
        else{
            refTrainees.child("tmp").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data: dataSnapshot.getChildren()){
                        addTrainDetails(data);
                    }
                }
            });
        }
    }

    /**
     * This function add the trains objects to arrays.
     *
     * @param data snapshot of the data the user got from firebase realtime database.
     */
    private void addTrainDetails(DataSnapshot data){
        Train tmp = data.getValue(Train.class);
        if(!tmp.getTrainName().equals("choose sport")){
            trainsData.add(tmp.toString());
            trainsObjArray.add(tmp);
            if(tmp.getTrainingTime().equals("-1") || tmp.getTrainingTime().equals("-2")){
                trainsTimes.add(tmp.getTrainingTime());
            }
            else{
                trainsTimes.add(String.valueOf(trainsTimes.size() + 1));
            }
            trainingDataSpinadp.notifyDataSetChanged();
        }
    }

    /**
     * This function is when add time button pressed, it is for choosing time of the trains and
     adding it to the firebase realtime database.
     * @param view
     */
    public void addTimeDay(View view) {
        if (daysSpin.getVisibility() == View.INVISIBLE){
            Train tmp = new Train(sports[sportsSpin.getSelectedItemPosition()]);
            if (uidUpdate != null){
                refTrainees.child(uidUpdate).child("trainsData").child(tmp.getTrainName())
                        .setValue(tmp);
            }
            else{
                refTrainees.child("tmp").child(tmp.getTrainingTime())
                        .setValue(tmp);
            }
            trainsObjArray.add(tmp);
            trainsData.add(tmp.toString());
            trainsTimes.add(tmp.getTrainingTime());
            trainingDataSpinadp.notifyDataSetChanged();
        }
        else if (coachNameEt.getText().toString().isEmpty() || sportsSpin.getSelectedItemPosition() == 0 || daysSpin.getSelectedItemPosition() == 0) {
            Toast.makeText(AddTrains.this, "fill the previous details.", Toast.LENGTH_LONG).show();
        }
        else {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfdays, int minute) {
                    hour = hourOfdays;
                    minutes = minute;

                    String trainingtime;
                    if (minutes < 10){
                        trainingtime = daysSpin.getSelectedItemPosition() + "" + hour + "0" + minutes;
                    }
                    else{
                        trainingtime = daysSpin.getSelectedItemPosition() + "" + hour + "" + minutes;
                    }
                    Train tmp = new Train(sports[sportsSpin.getSelectedItemPosition()], trainArea, trainingtime, coachNameEt.getText().toString());
                    if (uidUpdate != null){
                        refTrainees.child(uidUpdate).child("trainsData").child(String.valueOf(trainsData.size()))
                                .setValue(tmp);
                    }
                    else {
                        refTrainees.child("tmp").child(String.valueOf(trainsData.size()))
                                .setValue(tmp);
                    }
                    trainsData.add(tmp.toString());
                    trainsObjArray.add(tmp);
                    trainsTimes.add(String.valueOf(trainsTimes.size()));
                    trainingDataSpinadp.notifyDataSetChanged();
                    coachNameEt.setText("");
                }
            };
            TimePickerDialog timePickerDialog = new TimePickerDialog(AddTrains.this, onTimeSetListener, hour, minutes, true);
            timePickerDialog.show();
        }
    }

    /**
     * This function is for choosing the sport type for trainName attribute to create train object.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pos = position;
        if (parent == sportsSpin){
            trainName = sports[position];
            daysSpin.setVisibility(View.VISIBLE);
            coachNameEt.setVisibility(View.VISIBLE);
            if(trainName.equals("Tennis class")){
                trainArea = "Tennis court";
            }
            else if(trainName.equals("Volleyball class") || trainName.equals("Basketball class")){
                trainArea = "sports hall";
            }
            else if(trainName.equals("Pool class")){
                trainArea = "indoor pool";
            }
            else{
                trainArea = "free";
                daysSpin.setVisibility(View.INVISIBLE);
                coachNameEt.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * This function does nothing.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * This function is when save button pressed, it returns the user to the create user activity.
     *
     * @param view
     */
    public void saveTrains(View view) {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * This function is if the user pressed on a train in the listview for deleting the selected
     train.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this
     *            will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(AddTrains.this);
        adb.setMessage("Are you sure do you want to remove this train?");
        adb.setTitle("Warning!");

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (uidUpdate != null){
                    refTrainees.child(uidUpdate).child("trainsData").child(String.valueOf(position)).removeValue();
                }
                else {
                    refTrainees.child("tmp").child(trainsTimes.get(position)).removeValue();
                }
                trainsData.remove(position);
                trainsTimes.remove(position);
                trainsObjArray.remove(position);
                trainingDataSpinadp.notifyDataSetChanged();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog ad = adb.create();
        ad.show();
    }
}