package com.shortedwire.kiran.nfcplotter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran on 1/2/2018.
 */

public class ListFiles extends AppCompatActivity {

    int legacy = 0;
    int compare = 0;
    int commercial = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){

            }else{
                String key = extras.getString("LoadOption");
                if(key.equals("legacy")){
                    legacy = 1;
                }else if(key.equals("compare")){
                    compare = 1;
                }else if(key.equals("commercial")){
                    commercial = 1;

                }

            }
        }


        try {
            showFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showFiles() throws IOException {

        final  List<String> fileList = new ArrayList<String>();
        ListView listView = (ListView) findViewById(R.id.lv_list_files);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////////////////////////////for click listener//////////////////////////////////////////////////////
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               File selected = new File(fileList.get(position));
               FileInputStream fileInputStream = null;
               String text;
///////read out selected file and then write to string buffer////////////////
                try {
                    fileInputStream = openFileInput(selected.getName());
                    int read = -1;
                    StringBuffer buffer = new StringBuffer();
                    while((read = fileInputStream.read()) != -1 )
                        buffer.append((char) read);
                    text = buffer.toString();
                    fileChosen(text);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
///////////////////////////////////////////////////////////////////////////////////////////////////

        File directoryPath = getFilesDir();
        File[] files = directoryPath.listFiles();//stores all the files
///////////listing required files in the list////////////////////////////
        for(int i = 0;i<files.length;i++){
            if(legacy == 1) {
                if (files[i].getName().endsWith(".lgc")) {
                    fileList.add(files[i].getName());
                }
            }
            if(compare == 1){
                if (files[i].getName().endsWith(".cmp")) {
                    fileList.add(files[i].getName());
                }
            }
            if(commercial == 1){
                if (files[i].getName().endsWith(".com")) {
                    fileList.add(files[i].getName());
                }

            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(adapter);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    private void fileChosen(String texts){
        Intent intent = null;
        if(legacy == 1) {
            intent = new Intent(getApplicationContext(), ThermistorLActivity.class);
            legacy = 0;
        }else if( compare == 1){
            intent = new Intent(getApplicationContext(),CompareActivity.class);
            compare = 0;
        } else if( commercial == 1){
            intent = new Intent(getApplicationContext(),ThermistorCActivity.class);
            commercial = 0;
        }
        intent.putExtra("dataRecover",texts);
        startActivity(intent);
    }

}
