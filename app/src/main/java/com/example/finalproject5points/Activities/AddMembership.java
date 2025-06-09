package com.example.finalproject5points.Activities;

import static android.widget.Toast.LENGTH_LONG;
import static androidx.core.content.ContextCompat.startActivity;
import static com.example.finalproject5points.Activities.LogIn.currentTrainee;
import static com.example.finalproject5points.FBrefs.refAuth;
import static com.example.finalproject5points.FBrefs.refSports;
import static com.example.finalproject5points.FBrefs.refTrainees;
import static com.example.finalproject5points.FBrefs.storageReference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.finalproject5points.Objects.Membership;
import com.example.finalproject5points.Objects.Train;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddMembership extends AppCompatActivity {

    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 15/12/2024
     * This activity is for adding new users by the admin/update data of exist users.
     */
    ImageButton profileIv;
    EditText nameEt, emailEt, phoneEt;
    Button pickdataBtn, saveBtn;
    ToggleButton isGuard, isAdmin;
    ArrayList<Train> trainsObjArray;
    int CAMERA_REQUESTCODE = 11;
    int GALLERY_REQUESTCODE = 10;
    int PICK_TRAINS = 20;
    byte[] imageByte;
    Intent gi;
    String gotUid;

    /**
     * This function checks if it used for update data of exist user or for adding a new one.
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
        setContentView(R.layout.activity_add_membership);

        profileIv = findViewById(R.id.profileIV);
        nameEt = findViewById(R.id.NameET);
        emailEt = findViewById(R.id.emailET);
        phoneEt = findViewById(R.id.phoneNum);
        pickdataBtn = findViewById(R.id.addTrainsBtn);
        saveBtn = findViewById(R.id.saveBtn);
        isGuard = findViewById(R.id.guard_toggleBtn);
        isAdmin = findViewById(R.id.admin_toggleBtn);

        gi = getIntent();
        gotUid = gi.getStringExtra("uid");

        if (gotUid != null){
            setData();
        }

        trainsObjArray = new ArrayList<>();
    }

    /**
     * This function removes the temporary trains that created in firebase realtime database for a
     new membership.
     */
    @Override
    protected void onStop() {
        super.onStop();
        refTrainees.child("tmp").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    refTrainees.child("tmp").removeValue();
                }
            }
        });
    }

    /**
     * This function is to set the data of the user that the admin wants to update.
     */
    private void setData(){
        refTrainees.child(gotUid).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Membership tmp = dataSnapshot.getValue(Membership.class);
                nameEt.setText(tmp.getFullName());
                emailEt.setText(tmp.getEmail());
                phoneEt.setText(tmp.getPhone());
                isGuard.setChecked(tmp.isGuard());
                isAdmin.setChecked(tmp.isAdmin());
            }
        });
        setProfilePhoto();
        getTrainsExistTrainee();
    }

    /**
     * This function downloads from firebase realtime database exist trains data of the trainee that
     the admin wants to edit 
     */
    private void getTrainsExistTrainee(){
        refTrainees.child(gotUid).child("trainsData").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot ds = task.getResult();
                    for (DataSnapshot data : ds.getChildren()) {
                        trainsObjArray.add(data.getValue(Train.class));
                    }
                }
            }
        });
    }

    /**
     * This function is to download and set the photo of the user that admin wants to update.
     */
    private void setProfilePhoto(){
        try {
            File localfile = File.createTempFile(gotUid, ".jpeg");
            storageReference.child(gotUid).getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    String filePath = localfile.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    profileIv.setScaleType(ImageView.ScaleType.FIT_XY);
                    if (bitmap.getWidth() > bitmap.getHeight()){
                        profileIv.setRotation(90);
                    }
                    profileIv.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function is when the user pressed the save button, it checks that there is no missing
     data and creating the user with its data in firebase realtime database, uploads the profile
     photo to firebase storage, and returns to the main activity of the user.

     * @param view
     */
    public void saveClicked(View view) {
        if (nameEt.getText().toString().isEmpty() || emailEt.getText().toString().isEmpty()) {
            Toast.makeText(AddMembership.this, "Enter email and name correctly!", LENGTH_LONG).show();
        }
        if (phoneEt.getText().toString().isEmpty() || phoneEt.getText().toString().length() > 10) {
            Toast.makeText(AddMembership.this, "Enter valid phone number!", LENGTH_LONG).show();
        } else {
            if (trainsObjArray.isEmpty()){
                if(gotUid == null){
                    Toast.makeText(AddMembership.this, "enter the user's training data", LENGTH_LONG).show();
                }
            }
            else{
                if (imageByte != null){
                    if (gotUid == null){
                        storageReference.child(phoneEt.getText().toString()).putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.i("image_upload", "Image uploaded successfully.");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("image_upload", "Image upload failed: "+ e);
                            }
                        });
                    }
                    else{
                        storageReference.child(gotUid).putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.i("image_upload", "Image uploaded successfully.");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("image_upload", "Image upload failed: "+ e);
                            }
                        });
                    }
                    addNewMembership();
                }
                else{
                    Toast.makeText(this, "choose an image", LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * This function adds the new membership or updating the membership's data that the admin wanted
     to update.
     */
    private void addNewMembership(){
        Membership tmp = new Membership(
                nameEt.getText().toString(),
                emailEt.getText().toString(),
                phoneEt.getText().toString(),
                trainsObjArray,
                isGuard.isChecked(),
                isAdmin.isChecked()
        );
        if (gotUid == null){
            refTrainees.child(phoneEt.getText().toString()).setValue(tmp);
            refTrainees.child("tmp").removeValue();
            Toast.makeText(AddMembership.this, "membership added successfully!", LENGTH_LONG).show();
            finish();
            startActivity(getIntent());
        }
        else{
            refTrainees.child(gotUid).setValue(tmp);
            Toast.makeText(AddMembership.this, "membership updated successfully!", LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * This function is routes to adding trains data for the new user.
     * @param view
     */
    public void pickdataClicked(View view) {
        Intent intent = new Intent(AddMembership.this, AddTrains.class);
        if (gotUid != null){
            intent.putExtra("gotUid", gotUid);
        }
        startActivityForResult(intent, PICK_TRAINS);
    }

    /**
     * This function is when the user pressed the profile image button and shows option to add profile
     photo from gallery or taking a picture by camera.
     * @param view
     */
    public void profile_clicked(View view) {
        if(ContextCompat.checkSelfPermission(AddMembership.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddMembership.this, new String[] {Manifest.permission.CAMERA}, 100);
        }
        if(ContextCompat.checkSelfPermission(AddMembership.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddMembership.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
        AlertDialog.Builder adb = new AlertDialog.Builder(AddMembership.this);
        adb.setTitle("Attention!");
        adb.setMessage("choose adding profile photo option");

        adb.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, CAMERA_REQUESTCODE);
                }
                else{
                    Toast.makeText(AddMembership.this, "this isn't supported", LENGTH_LONG).show();
                }
            }
        });
        adb.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUESTCODE);
            }
        });

        AlertDialog ad = adb.create();
        ad.show();
    }

    /**
     * This function is to upload the selected profile photo to firebase storage or gets the trains
     that the admin added to the new user.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (requestCode == PICK_TRAINS && resultCode == RESULT_OK){
            if (gotUid == null){
                refTrainees.child("tmp").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot ds = task.getResult();
                            for (DataSnapshot data : ds.getChildren()) {
                                trainsObjArray.add(data.getValue(Train.class));
                            }
                        }
                    }
                });
            } else{
                getTrainsExistTrainee();
            }
        }
        if (requestCode == GALLERY_REQUESTCODE && resultCode == RESULT_OK && data != null){
            Uri urimage = data.getData();
            profileIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            profileIv.setImageURI(urimage);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), urimage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageByte = baos.toByteArray();
        }
        else if(requestCode == CAMERA_REQUESTCODE && resultCode == RESULT_OK && data != null){
            bitmap = (Bitmap) data.getExtras().get("data");
            profileIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            profileIv.setImageBitmap(bitmap);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageByte = baos.toByteArray();
        }
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
        menu.add("See all subscriptions");
        menu.add("Update a membership");
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
        if(st.equals("See all subscriptions")){
            intent = new Intent(this, seeMemberships.class);
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
        else if(st.equals("Update a membership")){
            intent = new Intent(this, seeMemberships.class);
        }
        else if(st.equals("Main")){
            intent = new Intent(this, MainMembershipActivity.class);
            intent.putExtra("update", true);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param keyCode The value in event.getKeyCode().
     * @param event Description of the key event.
     *
     * This function disabled the back button at bottom.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}