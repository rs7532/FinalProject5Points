package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.Activities.LogIn.currentTrainee;
import static com.example.finalproject5points.FBrefs.NfcStr;
import static com.example.finalproject5points.FBrefs.Uid;
import static com.example.finalproject5points.FBrefs.refAuth;
import static com.example.finalproject5points.FBrefs.refEncryptionKey;
import static com.example.finalproject5points.FBrefs.refGotin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject5points.CaptureAct;
import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class getIn_SportsCenter extends AppCompatActivity {
    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 2.0
     * @since 13/03/2024
     * this activity has the options to get in the sports center (with nfc or qr code).
     */
    boolean isGuard;
    TextView tV_name, back_Tv;
    ImageView iV_qrcode;
    Timer timer;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Button scanQrBtn;
    Integer encryptionKey;
    Context context;
    ValueEventListener valueEventListener;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     * this function getting the name of the user from firebase RTDB and present the name and the qr code.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_in_sports_center);

        tV_name = findViewById(R.id.tV_name);
        iV_qrcode = findViewById(R.id.iV_Qrcode);
        back_Tv = findViewById(R.id.back_Tv);
        scanQrBtn = findViewById(R.id.qrScanBtn);

        context = this;

        getEncryptionkey();
        getName();
        checkGuard();
        backBtn();

        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        @SuppressLint("SimpleDateFormat") String day = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).substring(0, 8);
        @SuppressLint("SimpleDateFormat") Integer time = Integer.valueOf(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).substring(9));

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                    if(data.containsKey(Uid)){
                        if((time - Integer.valueOf(data.get(Uid))) < 6){
                            Toast.makeText(getIn_SportsCenter.this, "Welcome!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        refGotin.child(day).addValueEventListener(valueEventListener);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.NFC}, 104);
        }
    }

    /**
     * This function gets the encryption key from firebase and starts creating the qr code
     */
    private void getEncryptionkey(){
        refEncryptionKey.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                encryptionKey = dataSnapshot.getValue(Integer.class);
                start_createQR();
            }
        });
    }

    /**
     * This function gets the name of the user that connected
     */
    private void getName(){
        currentTrainee.child("fullName").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                tV_name.setText(dataSnapshot.getValue(String.class));
            }
        });
    }

    /**
     * This function check if the connected user is a guard and if he is a guard it shows the option
     of scanning qr code of users for entrance.
     */
    private void checkGuard() {
        currentTrainee.child("guard").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    isGuard = (boolean) dataSnapshot.getValue();
                    showScan();
                }
            }
        });
    }

    /**
     * This function shows the option of scanning qr code of other users if the connected user is a guard.
     */
    private void showScan(){
        if (isGuard){
            scanQrBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function enables nfc reading.
     */
    private void enableRead() {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)){
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    /**
     * This function disables nfc reading.
     */
    private void disableRead() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * This function running the enabling of the nfc function while the activity in resuming.
     */
    @Override
    protected void onResume() {
        super.onResume();
        enableRead();
    }

    /**
     * This function running the disabling of the nfc function while the activity in on pause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableRead();
        refGotin.removeEventListener(valueEventListener);
    }

    /**
     *
     * @param intent The new intent that was started for the activity.
     *
     *  This function running the function that reads the data of the nfc that detected.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processNFC(intent);
    }

    /**
     *
     * @param intent - the intent that includes the nfc tag's information that detected
     *
     * This function is reading the data that the nfc tag includes.
     */
    @SuppressLint("NewApi")
    public void processNFC(Intent intent) {
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (messages != null) {
            for (Parcelable message : messages) {
                NdefMessage ndefMessage1 = (NdefMessage) message;
                for (NdefRecord record : ndefMessage1.getRecords()) {
                    switch (record.getTnf()) {
                        case NdefRecord.TNF_WELL_KNOWN:
                            if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                                String nfcData = new String(record.getPayload()).substring(3);
                                checkEntrance(nfcData);
                            }
                    }
                }
            }
        }
    }

    /**
     * @param nfcData contains the data of the scanned nfc tag
     *
     *  This function checks that the nfc tag that scanned is the tag of the sports center's entrance,
     if it is it uploads the uid of the user that entered the sports center with the date&time of the entrance.
     */
    private void checkEntrance(String nfcData){
        NfcStr.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);
                if (str.equals(nfcData)){
                    allowGetInNFC();
                    finish();
                }
            }
        });
    }

    /**
     * This function is creating the qr code with uid and day&time.
     */
    private void createQR() {
        BarcodeEncoder qr_code = new BarcodeEncoder();
        try {
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String time = df.format(Calendar.getInstance().getTime());
            String day = time.substring(0, 8);
            time = time.substring(9);
            Bitmap bitmap = qr_code.encodeBitmap(encryption(day + Uid + time), BarcodeFormat.QR_CODE, 400, 400);
            iV_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("qr code", e.toString());
        }
    }

    /**
     * This function makes qr code being created every 5 seconds within timer.
     */
    private void start_createQR(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getIn_SportsCenter.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createQR();
                    }
                });
            }
        };
        timer.schedule(task, 0, 5000);
    }

    /**
     *
     * @param toEncrypt - the data that the qr code includes
     * @return encrypted data
     *
     * This function encrypts the data of the qr code.
     */
    private String encryption(String toEncrypt){
        char character;
        char[] ch = new char[toEncrypt.length()];

        for(int i = 0; i < toEncrypt.length(); i++){
            character = toEncrypt.charAt(i);
            if (i < 8 || i > (toEncrypt.length() - 7)){
                character = (char) (character + encryptionKey);
            }
            ch[i] = character;
        }
        return String.valueOf(ch);
    }

    /**
     *
     * @param toDecrypt - the data that the qr code includes
     * @return decrypted data
     *
     * This function decrypts the data of the qr code.
     */
    private String decryption(String toDecrypt){
        char character;
        char[] ch = new char[toDecrypt.length()];

        for(int i = 0; i < toDecrypt.length(); i++){
            character = toDecrypt.charAt(i);
            if (i < 8 || i > (toDecrypt.length() - 7)) {
                character = (char) (character - encryptionKey);
            }
            ch[i] = character;
        }
        return String.valueOf(ch);
    }

    /**
     * This function creates the button to return to the main activity.
     */
    private void backBtn() {
        SpannableString ss = new SpannableString("<-Back");
        ClickableSpan span = new ClickableSpan() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View textView) {
                finish();
            }
        };
        ss.setSpan(span, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        back_Tv.setText(ss);
        back_Tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * @param view
     *
     * This function starts the scan of the qr code when the button pressed.
     */
    public void scanQr_Clicked(View view) {
        scanCode();
    }

    /**
     * This function generates the scan of the qr code
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    /**
     * This gets the data of the scanned qr code
     */
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result ->{
        if(result.getContents() != null){
            String scannedData = result.getContents();
            decodeData(scannedData);
        }
    });

    /**
     * @param scannedData - the data of the qr code that have been scanned.
     *
     * This function checks that the qr code is updated to the last few seconds.
     */
    private void decodeData(String scannedData){
        String decryptedStr = decryption(scannedData);

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String tmp = df.format(Calendar.getInstance().getTime());
        Integer current_day = Integer.valueOf(tmp.substring(0, 8));
        Integer current_time = Integer.valueOf(tmp.substring(9));

        Integer day = Integer.valueOf(decryptedStr.substring(0, 8));
        Integer time = Integer.valueOf(decryptedStr.substring(decryptedStr.length() - 6));

        if(current_day - day == 0){
            if (current_time - time < 6){
                allowGetInQR(decryptedStr.substring(8, decryptedStr.length() - 6), day, time);
            }
        }
    }

    /**
     * @param uid - uid of the user that it's qr code scanned
     * @param day - date of scan
     * @param time - time of scan
     *
     * This function uploads the uid of the user that entered the sports center with the date&time of
     the entrance when it was qr code option.
     */
    private void allowGetInQR(String uid, Integer day, Integer time) {
        refGotin.child(String.valueOf(day)).child(uid).setValue(String.valueOf(time));
        finish();
    }

    /**
     * This function uploads the uid of the user that entered the sports center with the date&time of
     the entrance when it was nfc tag option.
     */
    private void allowGetInNFC() {
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String tmp = df.format(Calendar.getInstance().getTime());
        Integer current_day = Integer.valueOf(tmp.substring(0, 8));
        Integer current_time = Integer.valueOf(tmp.substring(9));
        refGotin.child(String.valueOf(current_day)).child(refAuth.getUid()).setValue(String.valueOf(current_time));
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