package com.example.finalproject5points.Activities;

import static com.example.finalproject5points.Activities.LogIn.currentTrainee;
import static com.example.finalproject5points.FBrefs.Uid;
import static com.example.finalproject5points.FBrefs.refEncryptionKeys;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject5points.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class getIn_SportsCenter extends AppCompatActivity {
    /**
     * @author Roey Schwartz rs7532@bs.amalnet.k12.il
     * @version 1.0
     * @since 13/03/2024
     * this activity has the options to get in the sports center (with nfc or qr code).
     */
    TextView tV_name, back_Tv;
    ImageView iV_qrcode;
    Timer timer;
    Random rnd = new Random();
    final int encryptionKey = rnd.nextInt(100);
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

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

        currentTrainee.child("fullName").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                tV_name.setText(dataSnapshot.getValue(String.class));
            }
        });

        refEncryptionKeys.child(Uid).child("Key").setValue(encryptionKey);

        backBtn();
        start_createQR();


        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * This function enables nfc reading.
     */
    private void enableRead() {
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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
                                String st = new String(record.getPayload()).substring(3);
                                Toast.makeText(getIn_SportsCenter.this, st, Toast.LENGTH_LONG).show();
                            }
                    }
                }
            }
        }
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
            if (i < 8 || i > (toEncrypt.length() - 6)){
                character = (char) (character + encryptionKey);
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

}