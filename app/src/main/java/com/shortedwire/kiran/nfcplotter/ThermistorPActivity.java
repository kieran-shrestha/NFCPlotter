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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by kiran on 11/27/2017.
 */

public class ThermistorPActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    TextView tvthermistorp;
    SeekBar seekbar;

    DateTimeActivity dateTimeActivity = new DateTimeActivity();

    IntentFilter[] filters = new IntentFilter[1];
    int numberofData;

    public static int temperatureParsed[] = new int[100];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermistorp);

        context = this;
        tvthermistorp = (TextView) findViewById(R.id.tv_activity_thermistorp);
        seekbar = (SeekBar) findViewById(R.id.sb_activity_thermistorp);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawGraph(temperatureParsed,numberofData,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

        String header = text.substring(0, 18);   //this is the serial and name
        int intervalLog = text.charAt(18);

        numberofData = (payload.length - 20) / 3 - 1;
        StringBuilder sb = new StringBuilder();

        String[] dateTime = dateTimeActivity.getDateTime(numberofData,intervalLog);
//
//        Log.i("INFO","interval is "+intervalLog);
//        Log.i("INFO","Payload length "+payload.length);
//        Log.i("INFO","number of data "+ numberOfData);
//        Log.i("INFO","header "+header);
//        Log.i("INFO","content "+text);

        sb.append(header);
        for (int x = 19, y = 0; y < numberofData; x += 3, y++) {
            temperatureParsed[y] = Integer.parseInt(text.substring(x, x + 3));
//            Log.i("INFO",y+"value" +text.substring(x,x+3));
            //************************************************************************
            //************************Bijen's sensor code*****************************
            //************************************************************************/
            double tempConv = temperatureParsed[y] * 1000 / 2.700;
          //  int C = (int) (-0.01328 * tempConv + 1152.00357) / 10;    //for 100k
          //   int C = (int) (-0.01328*tempConv+527.85825)/10; // for 40k

            //VTDA(2) 017.12.08
           // int C = (int) (-(9.7892/10000)*tempConv + 268.19345);

            //crpped first one in my phone the picture
            int C = (int) (-.00885*tempConv + 262.0385);

            temperatureParsed[y] = C+15;
          //  Log.i("INFO"," temp "+y+ ' ' +C);

            sb.append(' ');
            sb.append(C+15);
            sb.append(".00");
            sb.append(dateTime[y]);//new line is added from date and time
        //    sb.append(dateTimeActivity.getDateTime());//new line is added from date and time
        }

        text = sb.toString();

        drawGraph(temperatureParsed, numberofData,0);
        TextView layout = findViewById(R.id.tv_activity_thermistorp);
        layout.setVisibility(View.VISIBLE);
        tvthermistorp.setText(text);
    }

    public int getMin(int yData[]){
        int Min = yData[0];
        for(int i = 1; i<yData.length;i++){
            if(yData[i]<Min){
                Min = yData[i];
            }
        }

        return Min;
    }

    public int getMax(int yData[]){
        int Max = yData[0];
        for(int i = 1; i<yData.length;i++){
            if(yData[i]>Max) {
                Max = yData[i];
            }
        }
        return Max;
    }


    public void drawGraph(int yData[], int numberOfData,int factors) {
        float radius = 5;

        int Min = getMin(yData);
        int Max = getMax(yData);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8/*1 /getResources().getDisplayMetrics().density*/);
        paint.setStyle(Paint.Style.STROKE);

        int x[] = getScreenSIze();
        int screenWidth = x[0];
        int screenHeight = x[1];

        int bmpxgap = (screenWidth - 100) / numberOfData;

        LinearLayout heightLayout = (LinearLayout) findViewById(R.id.layout_thermistorpparent);
        int headerLayoutParentHeight = heightLayout.getHeight();

        int bmpy = (headerLayoutParentHeight) / 2;

        Bitmap bmp = Bitmap.createBitmap(screenWidth, bmpy, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        int factor = 10;
        switch (factors){
            case 0 : factor = 10;
                break;
            case 1 : factor = 20;
                break;
            case 2 : factor = 25;
                break;

        }
///////this is axis/////////////////
        canvas.drawLine(50,bmpy-50, screenWidth-50,bmpy-50, paint);//x axis
        canvas.drawLine(50, bmpy-50, 50, 50, paint);

        paint.setStrokeWidth(1f);
        paint.setTextSize(30);
/////////////////////this si the max and min text/////////////////////////////
        canvas.drawText(Integer.toString(Min),10,(bmpy - 50)-Min*factor,paint);
        canvas.drawText(Integer.toString(Max),10,(bmpy - 50)-Max*factor,paint);

        int center = (Min+Max)/2;
        int gapY = (Max-center)/2;
        int j = 0;

        canvas.drawText(Integer.toString(center+gapY),10,(bmpy - 50)-(center+gapY)*factor,paint);
        canvas.drawText(Integer.toString(center-gapY),10,(bmpy - 50)-(center-gapY)*factor,paint);

/////////////draws lines according to the max - center gap and repeat///////////////
        //////////makes the horizontal dark lines////////////////////////////////
        //starts to write only after max data
        while((bmpy-50)-(Max+gapY*j)*factor > 50 ){
            canvas.drawText(Integer.toString(Max+gapY*j),10,(bmpy-50)-(Max+gapY*j)*factor ,paint);

            paint.setStrokeWidth(2f);
            paint.setColor(Color.BLACK);
            paint.setAlpha(75);
            canvas.drawLine(50, (bmpy-50)-(Max+gapY*j)*factor, screenWidth - 50, (bmpy-50)-(Max+gapY*j)*factor, paint);

            paint.setAlpha(255);
            paint.setStrokeWidth(1f);
            paint.setTextSize(30);

            j++;
        }


        //////make horizontal lines below min
        j = 0;
        while( (bmpy - 50) - (Min- ((center-Min)/2)*j) * factor < bmpy - 50){
            canvas.drawText(Integer.toString(Min- ((center-Min)/2)*j),10,(bmpy - 50) - (Min- ((center-Min)/2)*j) * factor ,paint);

            paint.setStrokeWidth(2f);
            paint.setColor(Color.BLACK);
            paint.setAlpha(75);
            canvas.drawLine(50,(bmpy - 50) - (Min- ((center-Min)/2)*j) * factor , screenWidth - 50,(bmpy - 50) - (Min- ((center-Min)/2)*j) * factor , paint);
            j++;
            paint.setAlpha(255);
            paint.setStrokeWidth(1f);
            paint.setTextSize(30);
        }

//////////writes the center text///////////////////////////////////
        canvas.drawText(Integer.toString(center),10,(bmpy - 50)-center*factor,paint);

//  drawing the min max  center lines
        paint.setStrokeWidth(4f);
        paint.setColor(Color.RED);
        canvas.drawLine(50, (bmpy - 50)-Min*factor, screenWidth - 50, (bmpy - 50)-Min*factor, paint);
        canvas.drawLine(50, (bmpy - 50)-Max*factor, screenWidth - 50, (bmpy - 50)-Max*factor, paint);
        canvas.drawLine(50, (bmpy - 50)-center*factor, screenWidth - 50, (bmpy - 50)-center*factor, paint);

        canvas.drawLine(50, (bmpy - 50)-(center+gapY)*factor, screenWidth - 50, (bmpy - 50)-(center+gapY)*factor, paint);
        canvas.drawLine(50, (bmpy - 50)-(center-gapY)*factor, screenWidth - 50, (bmpy - 50)-(center-gapY)*factor, paint);

        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLACK);
        paint.setAlpha(50);
/////////makes grid constant
        for(int i = 1 ;i<= 10;i++) {

            paint.setAlpha(20);
            canvas.drawLine(50, i*(bmpy - 50) / 10, screenWidth - 50, i*(bmpy - 50) / 10, paint);   //horizontal
            canvas.drawLine(i*(screenWidth-50)/10+50,bmpy-50,i*(screenWidth-50)/10+50,50,paint);
        }

        paint.setStrokeWidth(8);
        paint.setAlpha(255);
        paint.setColor(Color.BLUE);

        float prevstartx=0,prevstarty=0;

        for (int i = 0; i < numberOfData; i++) {
            canvas.drawCircle(50 + bmpxgap * i, (bmpy - 50) - yData[i] * factor, radius, paint);
            if(i%20 == 0){  //on every 10 data draw vertical line
                paint.setAlpha(255);
                paint.setStrokeWidth(1f);
                paint.setTextSize(20);

                canvas.drawText(Integer.toString(i),40+bmpxgap*i,(bmpy - 10)  ,paint);

                paint.setStrokeWidth(2f);
                paint.setColor(Color.BLACK);
                paint.setAlpha(100);
                canvas.drawLine(50 + bmpxgap * i, bmpy-50, 50 + bmpxgap * i, 50, paint);
                paint.setStrokeWidth(3);
                paint.setAlpha(255);
                paint.setColor(Color.BLUE);
            }
            if( i > 0){
                canvas.drawLine(prevstartx,prevstarty,(float)(50 + bmpxgap * i), (float)((bmpy - 50) - yData[i] * factor),paint);
            }
            prevstartx = 50 + bmpxgap * i;
            prevstarty = (bmpy - 50) - yData[i] * factor;
        }
        ImageView imageview = (ImageView) findViewById(R.id.iv_activity_thermistorp);
        imageview.setImageBitmap(bmp);
        imageview.setVisibility(View.VISIBLE);

        seekbar.setVisibility(View.VISIBLE);
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