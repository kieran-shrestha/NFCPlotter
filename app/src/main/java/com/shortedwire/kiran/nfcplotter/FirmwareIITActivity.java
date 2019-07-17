package com.shortedwire.kiran.nfcplotter;

/**
 * Created by kiran on 12/22/2017.
 */

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
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.lang.Math.abs;


/**
 * Created by kiran on 11/27/2017.
 */

public class FirmwareIITActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    TextView tvThermistorII;
    SeekBar seekbar;

    DateTimeActivity dateTimeActivity = new DateTimeActivity();

    String dataRecovered,NFCId;
    int recovery = 0;

    IntentFilter[] filters = new IntentFilter[1];
    int numberofData;

    public static int temperatureParsed[] = new int[100];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermistorvii);

        context = this;
        tvThermistorII = (TextView) findViewById(R.id.tv_activity_thermistor_vii);
        seekbar = (SeekBar) findViewById(R.id.sb_activity_thermistor_vii);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawGraph(temperatureParsed, numberofData, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){

            }else {
                dataRecovered = extras.getString("dataRecover");
                recovery = 1;
            }

        }

        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_thermistorvii_parent);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if(recovery == 1){
                    recovery = 0;
                    buildTagText(dataRecovered,0);
                }
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
            Tag tagId = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            NFCId = bytesToHexString(tagId.getId());
            Log.d("NFCID","tag id is " + NFCId);
            buildTagViews(msgs);
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return ;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, "ISO-8859-1");//*/ textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

       buildTagText(text,1);
    }

    public void buildTagText(String text,int writeFile){
        ///////////////write section
        FileOutputStream fileOutputStream = null;
        if(writeFile == 1) {
            try {
                fileOutputStream = openFileOutput("Encoded" + dateTimeActivity.getDateTimeFileName() + ".hxd", Context.MODE_PRIVATE);
                fileOutputStream.write(text.getBytes());
                Toast.makeText(this, "Saved !!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//////////////upto here is added

        String header = text.substring(0, 18);   //this is the serial and name
        int intervalLog = text.charAt(18);


        Log.d("Level1","interval of log is "+ intervalLog);

        numberofData = (text.length() - 20) / 3 - 1;
        StringBuilder sb = new StringBuilder();

        String[] dateTime = dateTimeActivity.getDateTime(numberofData, intervalLog*2);

        sb.append(header);
        for (int x = 19, y = 0; y < numberofData; x += 3, y++) {
            temperatureParsed[y] = text.charAt(x + 1);
            temperatureParsed[y] <<= 8;
            temperatureParsed[y] |= text.charAt(x);

            Log.d("Level1", "hex code" + temperatureParsed[y]);

            double analogVolt = 0;
            int C=0;
            double tempConv = 0;

            Log.d("Level1",NFCId);
            if(    NFCId.equals("0x560e010000a207e0")   //HAVE
                || NFCId.equals("0x520e010000a207e0")
                || NFCId.equals("0x36d3010000a207e0")
                || NFCId.equals("0x580e010000a207e0")   //HAVE
                || NFCId.equals("0x590e010000a207e0")   //have
            ) {
                //*******************For Commercial thermistor **************************/
                analogVolt = (900.0 * temperatureParsed[y] / 16384.0);              //gain 1
                tempConv = ((analogVolt / 270.0) * 100000);
                C = (int) (100 * ((1.0 / ((((Math.log10(tempConv / 100000.0) / Math.log10(2.718))) / 4250.0) + (1.0 / 298.15))) - 273.15));

            } else{
                //*******************For Printed thermistor **************************/
                analogVolt = (112.5 * temperatureParsed[y] / 16384.0);           //DIGAIN_8 used
                tempConv = ((analogVolt / 270.0) * 100000);

                if( NFCId.equals("0x4d0e010000a207e0"))
                    C = (int) (100 *  (0.028116 * tempConv - 462.2849));
                else if (NFCId.equals("0x2cd3010000a207e0"))    //HAVE
                    C = (int) (100 *  (0.039494 * tempConv - 651.5309));

                else if ( NFCId.equals("0x2ed3010000a207e0"))       //have
                    C = (int) (100 *  (0.0362 * tempConv - 545-189));
                else if ( NFCId.equals("0x570e010000a207e0"))   //HAVE
                    C = (int) (100 *  (0.0503 * tempConv - 679 - 54));
            }


            Log.d("Level1", "Voltage Drop " + analogVolt);
            Log.d("Level1", "Resistance " + tempConv);
            Log.d("Level1", "Temp in C " + C);

            sb.append(' ');
            sb.append(", R: ,");
            sb.append((int)tempConv);
            sb.append(", ");

            temperatureParsed[y] = C;   //this is final temperature

            sb.append(' ');
            sb.append(temperatureParsed[y] / 100);
            sb.append(".");
            sb.append(abs(temperatureParsed[y] % 100));
            temperatureParsed[y] /= 100;
            sb.append(dateTime[y]);//new line is added from date and time
        }

        text = sb.toString();


        drawGraph(temperatureParsed, numberofData, 0);

        TextView layout = findViewById(R.id.tv_activity_thermistor_vii);
        layout.setVisibility(View.VISIBLE);
        tvThermistorII.setText(text);
    }

    public int getMin(int yData[],int length) {
        int Min = yData[0];
        for (int i = 1; i < length; i++) {
            if (yData[i] < Min) {
                Min = yData[i];
            }
        }

        return Min;
    }

    public int getMax(int yData[],int length) {
        int Max = yData[0];
        for (int i = 1; i < length; i++) {
            if (yData[i] > Max) {
                Max = yData[i];
            }
        }
        return Max;
    }

    public void drawGraph(int yData[], int numberOfData, int factors) {
        float radius = 5;

        int Min = getMin(yData,numberOfData);
        int Max = getMax(yData,numberOfData);

        int oldMin = Min;
        int oldMax = Max;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8/*1 /getResources().getDisplayMetrics().density*/);
        paint.setStyle(Paint.Style.STROKE);

        int x[] = getScreenSIze();
        int screenWidth = x[0];
        int screenHeight = x[1];

        float bmpxgap = (screenWidth - 100) / numberOfData;

        LinearLayout heightLayout = (LinearLayout) findViewById(R.id.layout_thermistorvii_parent);
        int headerLayoutParentHeight = heightLayout.getHeight();

//        Log.i("INFO", "y" + headerLayoutParentHeight);

        int bmpy = (headerLayoutParentHeight) / 2;

        Bitmap bmp = Bitmap.createBitmap(screenWidth, bmpy, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        int factor = 10;
        switch (factors) {
            case 0:
                factor = 10;
                break;
            case 1:
                factor = 20;
                break;
            case 2:
                factor = 25;
                break;

        }

        int minSet = 0;

        if (Min < 0) {
            minSet = 1;
            for (int i = 0; i < numberOfData; i++) {
                yData[i] += abs(Min) + 30;
            }

        }

        Min = getMin(yData,numberOfData);
        Max = getMax(yData,numberOfData);


///////this is axis/////////////////
        canvas.drawLine(50, bmpy - 50, screenWidth - 50, bmpy - 50, paint);//x axis
        canvas.drawLine(50, bmpy - 50, 50, 50, paint);

        paint.setStrokeWidth(1f);
        paint.setTextSize(30);
/////////////////////this is the max and min text/////////////////////////////
        if( minSet ==0 ) {
            canvas.drawText(Integer.toString(Min), 10, (bmpy - 50) - Min * factor, paint);
            canvas.drawText(Integer.toString(Max), 10, (bmpy - 50) - Max * factor, paint);
        } else if( minSet ==1 ){
           // canvas.drawText(Integer.toString(oldMin), 5, (bmpy - 50) - Min * factor, paint);
          //  canvas.drawText(Integer.toString(oldMax), 5, (bmpy - 50) - Max * factor, paint);
        }

        int center;
        int gapY;
        int oldCenter;


        center = (Min + Max) / 2;
        oldCenter = (oldMin + oldMax )/2;
        gapY = (Max - center) / 2;
        int gapYmin = (center - Min )/2;
        if(gapYmin < 10)
             gapYmin += 10;
        if(gapY < 10 )
            gapY += 10;

        int j = 0;

        canvas.drawText(Integer.toString(oldCenter + gapY), 10, (bmpy - 50) - (center + gapY) * factor, paint);
        canvas.drawText(Integer.toString(oldCenter - gapY), 10, (bmpy - 50) - (center - gapY) * factor, paint);

/////////////draws lines according to the max - center gap and repeat///////////////
        //////////makes the horizontal dark lines////////////////////////////////
        //starts to write only after max data
        while ((bmpy - 50) - (Max + gapY * j) * factor > 50) {
            canvas.drawText(Integer.toString(oldMax + gapY * j), 10, (bmpy - 50) - (Max + gapY * j) * factor, paint);

            paint.setStrokeWidth(2f);
            paint.setColor(Color.BLACK);
            paint.setAlpha(75);
            canvas.drawLine(50, (bmpy - 50) - (Max + gapY * j) * factor, screenWidth - 50, (bmpy - 50) - (Max + gapY * j) * factor, paint);

            paint.setAlpha(255);
            paint.setStrokeWidth(1f);
            paint.setTextSize(30);

            j++;
            if (j > 100)
                break;
        }

        //////make horizontal lines below min
        j = 0;
        while ((bmpy - 50) - (Min - ((center - Min) / 2) * j) * factor < bmpy - 50) {
            canvas.drawText(Integer.toString(oldMin - ((oldCenter - oldMin) / 2) * j), 10, (bmpy - 50) - (Min - gapYmin * j) * factor, paint);

            paint.setStrokeWidth(2f);
            paint.setColor(Color.BLACK);
            paint.setAlpha(75);
            canvas.drawLine(50, (bmpy - 50) - (Min - gapYmin * j) * factor, screenWidth - 50, (bmpy - 50) - (Min - gapYmin * j) * factor, paint);
            j++;
            if (j > 100)
                break;
            paint.setAlpha(255);
            paint.setStrokeWidth(1f);
            paint.setTextSize(30);
        }

//////////writes the center text///////////////////////////////////
        canvas.drawText(Integer.toString(oldCenter), 10, (bmpy - 50) - center * factor, paint);

//  drawing the min max  center lines
        paint.setStrokeWidth(4f);
        paint.setColor(Color.RED);
        canvas.drawLine(50, (bmpy - 50) - Min * factor, screenWidth - 50, (bmpy - 50) - Min * factor, paint);
        canvas.drawLine(50, (bmpy - 50) - Max * factor, screenWidth - 50, (bmpy - 50) - Max * factor, paint);
        canvas.drawLine(50, (bmpy - 50) - center * factor, screenWidth - 50, (bmpy - 50) - center * factor, paint);

       // canvas.drawLine(50, (bmpy - 50) - (center + gapY) * factor, screenWidth - 50, (bmpy - 50) - (center + gapY) * factor, paint);
      //  canvas.drawLine(50, (bmpy - 50) - (center - gapY) * factor, screenWidth - 50, (bmpy - 50) - (center - gapY) * factor, paint);

        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLACK);
        paint.setAlpha(50);
/////////makes grid constant
        for (int i = 1; i <= 10; i++) {

            paint.setAlpha(20);
            canvas.drawLine(50, i * (bmpy - 50) / 10, screenWidth - 50, i * (bmpy - 50) / 10, paint);   //horizontal
            canvas.drawLine(i * (screenWidth - 50) / 10 + 50, bmpy - 50, i * (screenWidth - 50) / 10 + 50, 50, paint);
        }

        paint.setStrokeWidth(3);
        paint.setAlpha(255);
        paint.setColor(Color.BLUE);
        float prevstartx = 0, prevstarty = 0;


        for (int i = 0; i < numberOfData; i++) {
            canvas.drawCircle(50 + bmpxgap * i, (bmpy - 50) - yData[i] * factor, radius, paint);
            if (i % 20 == 0) {  //on every 10 data draw vertical line
                paint.setAlpha(255);
                paint.setStrokeWidth(1f);
                paint.setTextSize(20);

                canvas.drawText(Integer.toString(i), 40 + bmpxgap * i, (bmpy - 10), paint);

                paint.setStrokeWidth(2f);
                paint.setColor(Color.BLACK);
                paint.setAlpha(100);
                canvas.drawLine(50 + bmpxgap * i, bmpy - 50, 50 + bmpxgap * i, 50, paint);
                paint.setStrokeWidth(3);
                paint.setAlpha(255);
                paint.setColor(Color.BLUE);
            }
            if (i > 0) {
                canvas.drawLine(prevstartx, prevstarty, (float) (50 + bmpxgap * i), (float) ((bmpy - 50) - yData[i] * factor), paint);
            }
            prevstartx = 50 + bmpxgap * i;
            prevstarty = (bmpy - 50) - yData[i] * factor;
        }
        ImageView imageview = (ImageView) findViewById(R.id.iv_activity_thermistor_vii);
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