package com.shortedwire.kiran.nfcplotter;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button button_thermistor_C;
    Button button_thermistor_P;
    Button button_thermistor_I;
    Button button_thermistor_L;
    Button button_normal_T;
    Button button_Compare;
    Button button_Version2;
    Button button_LoadLegacy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_thermistor_C = findViewById(R.id.B_thermistor_C);
        button_thermistor_P = findViewById(R.id.B_thermistor_P);
        button_thermistor_I = findViewById(R.id.B_thermistor_I);
        button_thermistor_L = findViewById(R.id.B_thermistor_L);
        button_normal_T = findViewById(R.id.B_normal_tag);
        button_Compare = findViewById(R.id.B_Compare);
        button_Version2 = findViewById(R.id.B_hexDecode);
        button_LoadLegacy = findViewById(R.id.B_loadLegacy);

        button_thermistor_C.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(),ThermistorCActivity.class);
               startActivity(intent);
            }
        });

        button_thermistor_P.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ThermistorPActivity.class);
                startActivity(intent);
            }
        });

        button_thermistor_I.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ThermistorIActivity.class);
                startActivity(intent);
            }
        });

        button_thermistor_L.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ThermistorLActivity.class);
                startActivity(intent);
            }
        });

        button_normal_T.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),NormalTActivity.class);
                startActivity(intent);
            }
        });

        button_Compare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CompareActivity.class);
                startActivity(intent);
            }
        });

        button_Version2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),FirmwareIITActivity.class);
                startActivity(intent);
            }
        });

        button_LoadLegacy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ListLegacy.class);
                startActivity(intent);
            }
        });


    }




}


