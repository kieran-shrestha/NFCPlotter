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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by kiran on 12/12/2017.
 */

public class CompareActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    TextView tvCompare;

    IntentFilter[] filters = new IntentFilter[1];

    public static int temperatureParsed0[] = new int[500];
    public static int temperatureParsed1[] = new int[500];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);


        context = this;
        tvCompare = (TextView) findViewById(R.id.tv_activity_Compare);
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
        writeTagFilters = new IntentFilter[]{tagDetected};
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

        int y, z = payload.length / 31;
        int a, b;

        // Log.i("INFO","payload "+z);

        for (int x = 0; x < z; x++) {
            y = x * 31;
            a = Integer.parseInt(text.substring(y + 1, y + 2));
            // Log.i("INFO", "A" + a);
            b = Integer.parseInt(text.substring(y + 2, y + 3));
            // Log.i("INFO","B" + b);
            temperatureParsed0[x] = a * 10 + b;

            a = Integer.parseInt(text.substring(y + 8, y + 10));
            temperatureParsed1[x] = a;

        }

        drawGraph(temperatureParsed0, z, temperatureParsed1);
        // drawGraph(temperatureParsed1, z, 1);

        TextView layout = findViewById(R.id.tv_activity_Compare);
        layout.setVisibility(View.VISIBLE);
        tvCompare.setText(text);
    }

    public void drawGraph(int yData[], int numberOfData, int y2Data[]) {//color is only 1 and 2
        float radius = 5;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8/*1 /getResources().getDisplayMetrics().density*/);
        paint.setStyle(Paint.Style.STROKE);

        int x[] = getScreenSIze();
        int screenWidth = x[0];
        int screenHeight = x[1];

        float bmpxgap = (screenWidth - (float) 100.0) / numberOfData;

        LinearLayout heightLayout = (LinearLayout) findViewById(R.id.layout_Compare_parent);
        int headerLayoutParentHeight = heightLayout.getHeight();

//        Log.i("INFO","y"+headerLayoutParentHeight);

        int bmpy = (headerLayoutParentHeight) / 2;

        Bitmap bmp = Bitmap.createBitmap(screenWidth, bmpy, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        canvas.drawLine(50, bmpy - 50, screenWidth - 50, bmpy - 50, paint);//x axis
        canvas.drawLine(50, bmpy - 50, 50, 50, paint);

        paint.setStrokeWidth(1f);
        for(int i = 1 ;i<= 10;i++) {

            canvas.drawLine(50, i*(bmpy - 50) / 10, screenWidth - 50, i*(bmpy - 50) / 10, paint);
            canvas.drawLine(i*(screenWidth-50)/10+50,bmpy-50,i*(screenWidth-50)/10+50,50,paint);
        }
        paint.setStrokeWidth(8);
        paint.setColor(Color.BLUE);

        float prevstartx = 0, prevstarty = 0;
//        Log.i("INFO","xgap"+bmpxgap+"number"+numberOfData+"width"+screenWidth);

        for (int i = 0; i < numberOfData; i++) {
            canvas.drawCircle(50 + bmpxgap * i, (bmpy - 50) - yData[i] * 10, radius, paint);
            if (i > 0) {
                canvas.drawLine(prevstartx, prevstarty, (float) (50 + bmpxgap * i), (float) ((bmpy - 50) - yData[i] * 10), paint);
            }
            prevstartx = 50 + bmpxgap * i;
            prevstarty = (bmpy - 50) - yData[i] * 10;
        }

        paint.setColor(Color.RED);
        prevstartx = 0;
        prevstarty = 0;

        for (int i = 0; i < numberOfData; i++) {
            canvas.drawCircle(50 + bmpxgap * i, (bmpy - 50) - y2Data[i] * 10, radius, paint);
            if (i > 0) {
                canvas.drawLine(prevstartx, prevstarty, (float) (50 + bmpxgap * i), (float) ((bmpy - 50) - y2Data[i] * 10), paint);
            }
            prevstartx = 50 + bmpxgap * i;
            prevstarty = (bmpy - 50) - y2Data[i] * 10;
        }

        ImageView imageview = (ImageView) findViewById(R.id.iv_activity_Compare);
        imageview.setImageBitmap(bmp);
        imageview.setVisibility(View.VISIBLE);

    }

    private int[] getScreenSIze() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;

        int[] size = {w, h};
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
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }


    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }


}


