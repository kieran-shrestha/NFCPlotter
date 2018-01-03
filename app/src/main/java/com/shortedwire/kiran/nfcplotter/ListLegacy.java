package com.shortedwire.kiran.nfcplotter;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran on 1/2/2018.
 */

public class ListLegacy extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_legacy);

        try {
            showFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showFiles() throws IOException {

        final  List<String> fileList = new ArrayList<String>();
        ListView listView = (ListView) findViewById(R.id.lv_list_legacy);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               File selected = new File(fileList.get(position));
                FileInputStream fileInputStream = null;

                try {
                    fileInputStream = openFileInput(selected.getName());
                    int read = -1;
                    StringBuffer buffer = new StringBuffer();
                    while((read = fileInputStream.read()) != -1 )
                        buffer.append((char) read);
                    Log.i("FILE",buffer.toString());
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

//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = openFileInput("Legacy.dat");
//            int read = -1;
//            StringBuffer buffer = new StringBuffer();
//            while ((read = fileInputStream.read()) != -1)
//                buffer.append((char) read);
//            Log.i("INFO", buffer.toString());
//            TextView layout = findViewById(R.id.tv_activity_list_legacy);
//            layout.setVisibility(View.VISIBLE);
//            textView.setText(buffer);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        File directoryPath = getFilesDir();
        File[] files = directoryPath.listFiles();//stores all the files

        for(int i = 0;i<files.length;i++){
            if(files[i].getName().endsWith(".lgc")){
                fileList.add(files[i].getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(adapter);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    }

}
