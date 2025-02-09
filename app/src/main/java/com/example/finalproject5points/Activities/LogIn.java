package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.FBrefs.FBDB;
import static com.example.finalproject5points.FBrefs.Uid;
import static com.example.finalproject5points.FBrefs.refAuth;
import static com.example.finalproject5points.FBrefs.refSports;
import static com.example.finalproject5points.FBrefs.refTrainees;
import static com.example.finalproject5points.FBrefs.storageReference;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject5points.Objects.Membership;
import com.example.finalproject5points.Objects.Train;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LogIn extends AppCompatActivity {
    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 1/12/2024
     * this activity has login\register option with email and password
     */
    EditText email, password, phone;
    CheckBox rememeberMe;
    Button logInBtn;
    TextView registerTv;
    Intent intent;
    boolean registered = true, details_exist;
    public static DatabaseReference currentTrainee;
    String existEmail;
    byte[] image = null;
    ArrayList<Train> existTrains;


    /**
     * This function connects between java object to xml object, and signing in the user
     if he chose in the last connection to stay connected

     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        email = findViewById(R.id.ET_email);
        password = findViewById(R.id.ET_password);
        phone = findViewById(R.id.ET_phone);
        rememeberMe = findViewById(R.id.rememberCheckbox);
        logInBtn = findViewById(R.id.signInBtn);
        registerTv = findViewById(R.id.registerTv);
        existTrains = new ArrayList<>();

        intent = new Intent(LogIn.this, MainMembershipActivity.class);

        SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
        String emailtmp = settings.getString("Email", "");
        if(!emailtmp.isEmpty()){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting...");
            pd.show();
            refAuth.signInWithEmailAndPassword(emailtmp, settings.getString("Password", "")).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LogIn.this, "Connected!", Toast.LENGTH_LONG).show();
                        Log.i("Login act", "Login success");
                        currentTrainee = refTrainees.child(refAuth.getUid());
                        pd.dismiss();
                        startActivity(intent);
                    }
                }
            });
        }

        register_option();
    }

    /**
     * This function is making the activity a register activity for new users.
     */
    private void register_option() {
        SpannableString ss = new SpannableString("Don't have an account?  Register here!");
        ClickableSpan span = new ClickableSpan() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View textView) {
                phone.setVisibility(View.VISIBLE);
                logInBtn.setText("Register");
                registered=false;
                login_option();
            }
        };
        ss.setSpan(span, 24, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerTv.setText(ss);
        registerTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * This function is making the activity a login activity for exist users.
     */
    private void login_option() {
        SpannableString ss = new SpannableString("Already have an account?  Login here!");
        ClickableSpan span = new ClickableSpan() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View textView) {
                phone.setVisibility(View.INVISIBLE);
                logInBtn.setText("Log in");
                registered=true;
                register_option();
            }
        };
        ss.setSpan(span, 26, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerTv.setText(ss);
        registerTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * This activity works when the login/register button pressed and singing in the user to the app.
     * @param view
     */
    public void Clicked(View view) {
        String emailtmp = email.getText().toString();
        String passwordtmp = password.getText().toString();

        if (!registered) {
            String phonetmp = phone.getText().toString();
            if (phonetmp.isEmpty() || emailtmp.isEmpty() || passwordtmp.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            }
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting...");
            pd.show();

            if (rememeberMe.isChecked()) {
                SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Email", emailtmp);
                editor.putString("Password", passwordtmp);
                editor.commit();
            } else {
                SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Email", "");
                editor.putString("Password", "");
                editor.commit();
            }

            refTrainees.child(phonetmp).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dS) {
                    if (dS.exists()) {
                        details_exist = true;
                        existEmail = (String) dS.child("email").getValue();
                        if (emailtmp.equals(existEmail)) {
                            for (DataSnapshot data : dS.child("trainsData").getChildren()){
                                existTrains.add(data.getValue(Train.class));
                            }
                            createUser(emailtmp, passwordtmp, pd);
                        } else {
                            Toast.makeText(LogIn.this, "The email doesn't match, try again!", Toast.LENGTH_LONG).show();
                            pd.dismiss();
                        }
                    } else {
                        details_exist = false;
                        pd.dismiss();
                        Toast.makeText(LogIn.this, "phone doesn't exist please contact the center management", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("phone finder", e.toString());
                }
            });
        } else {
            if (emailtmp.isEmpty() || passwordtmp.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            } else {
                ProgressDialog pd = new ProgressDialog(this);
                pd.setTitle("Connecting...");
                pd.show();

                if (rememeberMe.isChecked()) {
                    SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("Email", emailtmp);
                    editor.putString("Password", passwordtmp);
                    editor.commit();
                } else {
                    SharedPreferences settings = getSharedPreferences("user details", MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("Email", "");
                    editor.putString("Password", "");
                    editor.commit();
                }

                refAuth.signInWithEmailAndPassword(emailtmp, passwordtmp).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LogIn.this, "Connected!", Toast.LENGTH_LONG).show();
                            Log.i("Login act", "Login success");
                            currentTrainee = refTrainees.child(refAuth.getUid());
                            startActivity(intent);
                        } else {
                            Toast.makeText(LogIn.this, "Wrong email or password, try again!", Toast.LENGTH_LONG).show();
                            Log.e("LogIn act", String.valueOf(task.getException()));
                        }
                    }
                });
            }
        }
    }

    /**
     * This function is creating the user in the firebase Authentication, and update the name of the branch of the
     user in the firebase realtime database to the uid that he got.
     *
     * @param emailtmp gets the email that the user entered.
     * @param passwordtmp gets the password that the user entered.
     * @param pd gets the progress bar that created for dismissing it after the sign in.
     */
    private void createUser(String emailtmp, String passwordtmp, ProgressDialog pd){
        refAuth.createUserWithEmailAndPassword(emailtmp, passwordtmp).addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(LogIn.this, "Connected!", Toast.LENGTH_LONG).show();
                    Log.i("Login act", "create user success");
                    Uid = refAuth.getUid();
                    currentTrainee = refTrainees.child(Uid);
                    updateBranchName();
                    updatePhotoFBName();
                    addTraineeTrainsData();
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * This function updates the branch name in firebase realtime database to the uid of the new user.
     */
    private void updateBranchName(){
        refTrainees.child(phone.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Membership tmp = dataSnapshot.getValue(Membership.class);
                refTrainees.child(phone.getText().toString()).removeValue();
                currentTrainee.setValue(tmp);
            }
        });
    }

    /**
     * This function updates the branch name of the user's profile image in firebase storage to
     the uid of the new user.
     */
    private void updatePhotoFBName(){
        try {
            File localfile = File.createTempFile(Uid, "jpeg");
            storageReference.child(phone.getText().toString()).getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    String filePath = localfile.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    image = baos.toByteArray();
                    uploadPhoto();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function is uploading the profile photo of the new user with it's uid.
     */
    private void uploadPhoto(){
        storageReference.child(Uid).putBytes(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("image with uid", "image uploaded successfully with the uid.");
                storageReference.child(phone.getText().toString()).delete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("image with uid", e.toString());
            }
        });
        image = null;
    }

    /**
     * This function is adding the user's training data for each branch suitable for the type
     of sport that exists.
     */
    private void addTraineeTrainsData(){
        for (int i = 0; i < existTrains.size(); i++){
            refSports.child(existTrains.get(i).getTrainName()).child(Uid).child(existTrains.get(i).getTrainingTime()).setValue(existTrains.get(i));
        }
    }
}