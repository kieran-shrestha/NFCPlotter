package com.shortedwire.kiran.nfcplotter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by kiran on 11/27/2017.
 */

public class ThermistorIActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    TextView tvthermistori;

    IntentFilter[] filters = new IntentFilter[1];

    public static int temperatureParsed[] = new int[100];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermistori);


            context = this;
            tvthermistori = (TextView) findViewById(R.id.tv_activity_thermistori);
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);

            if (nfcAdapter == null) {
                // Stop here, we definitely need NFC
                Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
                finish();
            }

            filters[0] = new IntentFilter();
            filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filters[0].addCategory(Intent.CATEGORY_DEFAULT);
            readFromIntent(getIntent());

            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[] { tagDetected };
        }

        /******************************************************************************
         **********************************Read From NFC Tag***************************
         ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        // Log.i("INFO","Payload length "+payload.length);

        int numberOfData = payload.length / 3 -2;
        StringBuilder sb = new StringBuilder();

        // Log.i("INFO","number of data "+ numberOfData);
        // Log.i("INFO","content"+text);

        for(int x = 1,y = 0;y<numberOfData;x+=3,y++){
            temperatureParsed[y] = Integer.parseInt(text.substring(x,x+2));

            //Log.i("INFO"," temp "+C);

            sb.append(' ');
            sb.append( temperatureParsed[y]);
            sb.append(".00,15:34,2017/11/28\n");
        }

        text = sb.toString();
        Log.i("INFO","content"+text);
        // drawGraph(temperatureParsed,numberOfData);
        TextView layout = findViewById(R.id.tv_activity_thermistori);
        layout.setVisibility(View.VISIBLE);
        tvthermistori.setText(text);
    }

    public void drawGraph(int yData[],int numberOfData){
        float radius = 5;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8/*1 /getResources().getDisplayMetrics().density*/);
        paint.setStyle(Paint.Style.STROKE);

        int x[] = getScreenSIze();
        int  screenWidth = x[0];
        int screenHeight = x[1];

        Bitmap bmp = Bitmap.createBitmap(screenWidth,screenHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);

        float bmpxgap = (bmp.getWidth()-100)/numberOfData;
        float bmpy = bmp.getHeight();

        canvas.drawLine(45,bmpy/2,bmp.getWidth()-50,bmpy/2,paint);//x axis
        canvas.drawLine(45,bmpy/2,45,bmpy/2-1000,paint);
        paint.setColor(Color.BLUE);
        for(int i = 0;i<numberOfData;i++) {
            canvas.drawCircle(50+bmpxgap*i, bmpy / 2 - ( 500 - yData[i]), radius, paint);
        }
        ImageView imageview =   (ImageView) findViewById(R.id.graph_view);
        imageview.setImageBitmap(bmp);
        imageview.setVisibility(View.VISIBLE);

    }

    private int[] getScreenSIze(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;

        int[] size={w,h};
        return size;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }



}
