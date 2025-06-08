package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.FBrefs.refAuth;
import static com.example.finalproject5points.FBrefs.refTrainees;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject5points.CustomAdapterMembership;
import com.example.finalproject5points.Objects.Membership;
import com.example.finalproject5points.Objects.Train;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class seeMemberships extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 30/1/2025
     * this activity is to see all the exist users for see/update their data.
     */
    Spinner filterSpin;
    ListView membershipLv;
    ArrayList<String> membershipNamesAl, membershipData, uids;
    Intent gi;
    boolean toUpdate;
    TextView tv;

    /**
     * This function gets all users from firebase realtime database, checks if the activity is for
     see users or update them.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_memberships);

        filterSpin = findViewById(R.id.filter_spin);
        membershipLv = findViewById(R.id.membershipLv);
        tv = findViewById(R.id.seeAndUpdate_tv);

        gi = getIntent();
        toUpdate = gi.getBooleanExtra("update", false);

        membershipNamesAl = new ArrayList<>();
        membershipData = new ArrayList<>();
        uids = new ArrayList<>();


        CustomAdapterMembership adp1 = new CustomAdapterMembership(this, membershipNamesAl);
        membershipLv.setAdapter(adp1);
        membershipLv.setOnItemClickListener(this);
        membershipLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (toUpdate){
            membershipLv.setOnItemLongClickListener(this);
            tv.setText("Update Memberships");
        }
        else{
            tv.setText("All Memberships");
        }

        refTrainees.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                for(DataSnapshot Memberdata : dataSnapshot.getChildren()){
                    Membership membertmp = Memberdata.getValue(Membership.class);
                    uids.add(Memberdata.getKey());
                    ArrayList<Train> traintmp = new ArrayList<>();
                    for (DataSnapshot Traindata : Memberdata.child("trainsData").getChildren()){
                        traintmp.add(Traindata.getValue(Train.class));
                    }
                    membertmp.setTrainsData(traintmp);
                    membershipData.add(membertmp.toString());
                    membershipNamesAl.add(membertmp.getFullName());
                }
                adp1.notifyDataSetChanged();
            }
        });
    }

    /**
     * This function shows all the selected user's data - personal data and trains data.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this
     *            will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(seeMemberships.this);
        adb.setMessage(membershipData.get(position));
        AlertDialog ad = adb.create();
        ad.show();
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
        menu.add("Main");
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
    @SuppressLint("SetTextI18n")
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
        else if(st.equals("Main")){
            intent = new Intent(this, MainMembershipActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function is when the activity used for update users, by pressing long press on a user it
     gets it uid and routes to the update user activity.
     *
     * @param parent The AbsListView where the click happened
     * @param view The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     *
     * @return false
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AddMembership.class);
        intent.putExtra("uid", uids.get(position));
        startActivity(intent);
        return false;
    }
}